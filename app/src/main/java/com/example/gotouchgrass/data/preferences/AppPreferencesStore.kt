package com.example.gotouchgrass.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.goTouchGrassAppDataStore by preferencesDataStore(name = "go_touch_grass_app")

private object Keys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")

    // Active trip persistence
    val TRIP_ACTIVE = booleanPreferencesKey("trip_active")
    val TRIP_START_MS = longPreferencesKey("trip_start_ms")
    val TRIP_ROUTE_ID = stringPreferencesKey("trip_route_id")       // empty = free-roam
    val TRIP_ROUTE_NAME = stringPreferencesKey("trip_route_name")
    val TRIP_DISTANCE_M = floatPreferencesKey("trip_distance_m")
    val TRIP_CAPTURES = intPreferencesKey("trip_captures")
    val TRIP_XP = intPreferencesKey("trip_xp")
}

/**
 * Local preferences for instant UI (theme, sound) without waiting on the network.
 */
class AppPreferencesStore(context: Context) {

    private val dataStore = context.applicationContext.goTouchGrassAppDataStore

    val darkModeFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.DARK_MODE] ?: false }

    val soundEffectsFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.SOUND_EFFECTS] ?: true }

    suspend fun readDarkMode(): Boolean = darkModeFlow.first()

    suspend fun readSoundEffectsEnabled(): Boolean = soundEffectsFlow.first()

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }

    // ── Trip persistence ──────────────────────────────────────────────────────

    data class SavedTripState(
        val active: Boolean,
        val startMs: Long,
        val routeId: String,
        val routeName: String,
        val distanceMeters: Float,
        val captures: Int,
        val xp: Int
    )

    val savedTripFlow: Flow<SavedTripState> = dataStore.data.map { prefs ->
        SavedTripState(
            active = prefs[Keys.TRIP_ACTIVE] ?: false,
            startMs = prefs[Keys.TRIP_START_MS] ?: 0L,
            routeId = prefs[Keys.TRIP_ROUTE_ID] ?: "",
            routeName = prefs[Keys.TRIP_ROUTE_NAME] ?: "",
            distanceMeters = prefs[Keys.TRIP_DISTANCE_M] ?: 0f,
            captures = prefs[Keys.TRIP_CAPTURES] ?: 0,
            xp = prefs[Keys.TRIP_XP] ?: 0
        )
    }

    suspend fun readSavedTrip(): SavedTripState = savedTripFlow.first()

    suspend fun saveTripState(
        active: Boolean,
        startMs: Long,
        routeId: String,
        routeName: String,
        distanceMeters: Float,
        captures: Int,
        xp: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.TRIP_ACTIVE] = active
            prefs[Keys.TRIP_START_MS] = startMs
            prefs[Keys.TRIP_ROUTE_ID] = routeId
            prefs[Keys.TRIP_ROUTE_NAME] = routeName
            prefs[Keys.TRIP_DISTANCE_M] = distanceMeters
            prefs[Keys.TRIP_CAPTURES] = captures
            prefs[Keys.TRIP_XP] = xp
        }
    }

    suspend fun clearTripState() {
        dataStore.edit { prefs ->
            prefs[Keys.TRIP_ACTIVE] = false
            prefs[Keys.TRIP_START_MS] = 0L
            prefs[Keys.TRIP_ROUTE_ID] = ""
            prefs[Keys.TRIP_ROUTE_NAME] = ""
            prefs[Keys.TRIP_DISTANCE_M] = 0f
            prefs[Keys.TRIP_CAPTURES] = 0
            prefs[Keys.TRIP_XP] = 0
        }
    }
}
