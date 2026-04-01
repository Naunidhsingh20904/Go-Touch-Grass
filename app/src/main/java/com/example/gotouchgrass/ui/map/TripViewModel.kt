package com.example.gotouchgrass.ui.map

import android.content.Context
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.data.preferences.AppPreferencesStore
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.LatLng
import com.example.gotouchgrass.domain.RouteStopMapMarker
import com.example.gotouchgrass.domain.TripSummary
import com.example.gotouchgrass.domain.TripZone
import com.example.gotouchgrass.service.TripForegroundService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TripViewModel(
    private val repository: GoTouchGrassRepository,
    private val prefsStore: AppPreferencesStore,
    private val currentUserId: String?,
    private val appContext: Context
) : ViewModel() {

    // ── Observable state ──────────────────────────────────────────────────────

    var isActive by mutableStateOf(false)
        private set

    var elapsedSeconds by mutableLongStateOf(0L)
        private set

    var distanceMeters by mutableFloatStateOf(0f)
        private set

    var xpEarned by mutableIntStateOf(0)
        private set

    var captureCount by mutableIntStateOf(0)
        private set

    var activeRouteName by mutableStateOf<String?>(null)
        private set

    var routeStopMarkers by mutableStateOf<List<RouteStopMapMarker>>(emptyList())
        private set

    var showCelebration by mutableStateOf(false)
        private set

    var showSummary by mutableStateOf(false)
        private set

    var lastSummary by mutableStateOf<TripSummary?>(null)
        private set

    var levelUp by mutableStateOf(false)
        private set

    var newLevel by mutableIntStateOf(0)
        private set

    var streakDays by mutableIntStateOf(0)
        private set

    // Set by MapScreen so it can refresh header + stats after Supabase writes complete
    var onTripSaved: (() -> Unit)? = null

    // ── Internal state ────────────────────────────────────────────────────────

    private var tripStartMs = 0L
    private var tripStartIso = ""
    private var lastKnownLocation: com.google.android.gms.maps.model.LatLng? = null
    private var timerJob: Job? = null
    private var zones: List<TripZone> = emptyList()

    // zoneId → accumulated dwell seconds during this trip
    private val zoneDwellMap = mutableMapOf<Long, Long>()

    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    // ── Init: restore persisted trip on process restart ───────────────────────

    init {
        viewModelScope.launch {
            // Load current streak for display
            if (currentUserId != null) {
                repository.getStreakData(currentUserId).onSuccess { s ->
                    streakDays = s.currentDays
                }
            }
        }
        viewModelScope.launch {
            val saved = prefsStore.readSavedTrip()
            if (saved.active && saved.startMs > 0L) {
                val nowMs = System.currentTimeMillis()
                tripStartMs = saved.startMs
                tripStartIso = isoFromEpoch(saved.startMs)
                distanceMeters = saved.distanceMeters
                captureCount = saved.captures
                elapsedSeconds = (nowMs - saved.startMs) / 1000L
                activeRouteName = saved.routeName.ifEmpty { null }
                xpEarned = saved.xp
                isActive = true
                loadZones()
                startTimer()
            }
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun startFreeRoamTrip() = startTrip(routeItem = null)

    fun startRouteTrip(routeItem: ExploreRouteItem) = startTrip(routeItem = routeItem)

    fun onCapture(xpAwarded: Int) {
        if (!isActive) return
        captureCount++
        xpEarned += xpAwarded
        persistTripState()
    }

    fun onLocationUpdate(newLatLng: com.google.android.gms.maps.model.LatLng) {
        if (!isActive) return

        val prev = lastKnownLocation
        if (prev != null) {
            val result = FloatArray(1)
            Location.distanceBetween(
                prev.latitude, prev.longitude,
                newLatLng.latitude, newLatLng.longitude,
                result
            )
            val delta = result[0]
            // Ignore GPS noise below 2m and teleports above 200m
            if (delta in 2f..200f) {
                distanceMeters += delta
                // XP from distance: 1 XP per 10 metres
                val newDistanceXp = (distanceMeters / 10f).toInt()
                val captureXp = captureCount * 120
                xpEarned = captureXp + newDistanceXp
            }
        }
        lastKnownLocation = newLatLng

        // Update zone dwell: add 1 second to the zone the user is currently in
        val domainLatLng = LatLng(newLatLng.latitude, newLatLng.longitude)
        val currentZone = zones.firstOrNull { zone -> isPointInPolygon(domainLatLng, zone.polygon) }
        if (currentZone != null) {
            zoneDwellMap[currentZone.id] = (zoneDwellMap[currentZone.id] ?: 0L) + 5L
        }

        persistTripState()
    }

    fun endTrip() {
        if (!isActive) return

        timerJob?.cancel()
        isActive = false

        val endMs = System.currentTimeMillis()
        val endIso = isoFromEpoch(endMs)
        val durationSec = elapsedSeconds.coerceAtLeast(1)

        val dominantZoneId = zoneDwellMap.entries.maxByOrNull { it.value }?.key

        val xpBeforeTrip = xpEarned  // local display XP; real level-up checked after save
        val summary = TripSummary(
            durationSec = durationSec,
            distanceMeters = distanceMeters,
            captureCount = captureCount,
            xpEarned = xpEarned,
            routeName = activeRouteName,
            dominantZoneId = dominantZoneId
        )
        lastSummary = summary
        // Show celebration overlay first; summary shown after user dismisses it
        if (summary.xpEarned > 0) showCelebration = true else showSummary = true

        // Stop foreground service
        stopTripService()

        // Capture values before resetTripState() zeroes them
        val distanceSnapshot = distanceMeters

        // Save to backend, then notify UI to refresh
        val uid = currentUserId
        if (uid != null) {
            viewModelScope.launch {
                repository.recordVisitSession(
                    userId = uid,
                    startedAtIso = tripStartIso,
                    endedAtIso = endIso,
                    durationSec = durationSec,
                    dominantZoneId = dominantZoneId
                )
                // Only add the walk-distance portion of XP here;
                // capture XP is already saved per-capture in recordCaptureByPlaceId
                val distanceXp = (distanceSnapshot / 10f).toInt()
                if (distanceXp > 0) {
                    repository.addXpForTrip(uid, distanceXp)
                }
                // Update daily streak
                val newStreak = repository.updateDailyExploreStreak(uid).getOrNull() ?: 0
                streakDays = newStreak

                // Detect level-up (every 1000 XP = 1 level)
                val freshUser = repository.getUser(uid).getOrNull()
                if (freshUser != null) {
                    val freshLevel = freshUser.level
                    // We compute old level from XP before the trip's distance portion
                    val oldTotalXp = (freshUser.xpTotal - distanceXp).coerceAtLeast(0)
                    val oldLevel = (oldTotalXp / 1000) + 1
                    if (freshLevel > oldLevel) {
                        newLevel = freshLevel
                        levelUp = true
                    }
                }

                // Notify after all writes are done so UI shows correct values
                onTripSaved?.invoke()
            }
        }

        viewModelScope.launch { prefsStore.clearTripState() }
        resetTripState()
    }

    fun dismissCelebration() {
        showCelebration = false
        showSummary = true   // chain into summary
    }

    fun dismissSummary() {
        showSummary = false
    }

    fun dismissLevelUp() {
        levelUp = false
    }

    fun updateRouteStopMarkers(markers: List<RouteStopMapMarker>) {
        routeStopMarkers = markers
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun startTrip(routeItem: ExploreRouteItem?) {
        if (isActive) return

        tripStartMs = System.currentTimeMillis()
        tripStartIso = isoFromEpoch(tripStartMs)
        elapsedSeconds = 0L
        distanceMeters = 0f
        captureCount = 0
        xpEarned = 0
        zoneDwellMap.clear()
        lastKnownLocation = null
        activeRouteName = routeItem?.title
        routeStopMarkers = emptyList()
        isActive = true

        viewModelScope.launch { loadZones() }

        if (routeItem != null) {
            loadRouteStops(routeItem)
        }

        startTimer()
        startTripService()
        persistTripState()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startOffset = elapsedSeconds
            val startTime = System.currentTimeMillis() - (startOffset * 1000L)
            while (true) {
                delay(1_000L)
                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000L
            }
        }
    }

    private suspend fun loadZones() {
        zones = repository.getZonesForTrip().getOrNull() ?: emptyList()
    }

    private fun loadRouteStops(routeItem: ExploreRouteItem) {
        val routeIdLong = routeItem.id.toLongOrNull() ?: return
        viewModelScope.launch {
            val stops = repository.getRouteStopLandmarks(routeIdLong).getOrNull() ?: return@launch
            // Markers will be resolved by MapScreen via Places API since we only have place_ids
            // Emit them as unresolved so MapScreen can batch-fetch lat/lngs
            _pendingRouteStopPlaceIds = stops
        }
    }

    // Shared with MapScreen for Places API resolution
    var _pendingRouteStopPlaceIds: List<Pair<Long, String>> by mutableStateOf(emptyList())
        private set

    fun clearPendingRouteStops() {
        _pendingRouteStopPlaceIds = emptyList()
    }

    private fun resetTripState() {
        elapsedSeconds = 0L
        distanceMeters = 0f
        captureCount = 0
        xpEarned = 0
        activeRouteName = null
        routeStopMarkers = emptyList()
        zoneDwellMap.clear()
        lastKnownLocation = null
        zones = emptyList()
    }

    private fun persistTripState() {
        viewModelScope.launch {
            prefsStore.saveTripState(
                active = isActive,
                startMs = tripStartMs,
                routeId = "",
                routeName = activeRouteName ?: "",
                distanceMeters = distanceMeters,
                captures = captureCount,
                xp = xpEarned
            )
        }
    }

    private fun startTripService() {
        val intent = TripForegroundService.buildStartIntent(appContext, tripStartMs)
        ContextCompat.startForegroundService(appContext, intent)
    }

    private fun stopTripService() {
        appContext.stopService(
            android.content.Intent(appContext, TripForegroundService::class.java)
        )
    }

    private fun isoFromEpoch(epochMs: Long): String {
        val odt = OffsetDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(epochMs),
            ZoneOffset.UTC
        )
        return odt.format(isoFormatter)
    }

    // ── Point-in-polygon (ray casting) ────────────────────────────────────────

    private fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val xi = polygon[i].longitude
            val yi = polygon[i].latitude
            val xj = polygon[j].longitude
            val yj = polygon[j].latitude
            val intersect = ((yi > point.latitude) != (yj > point.latitude)) &&
                    (point.longitude < (xj - xi) * (point.latitude - yi) / (yj - yi) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }
}
