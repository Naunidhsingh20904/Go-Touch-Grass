package com.example.gotouchgrass.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.gotouchgrass.data.FakeMapRepository
import com.example.gotouchgrass.data.FakeProfileRepository
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.domain.CollectedLandmark
import com.example.gotouchgrass.domain.LandmarkLeaderboardEntry
import com.example.gotouchgrass.domain.LandmarkOwnershipSummary
import com.example.gotouchgrass.domain.MapModel
import com.example.gotouchgrass.domain.RouteStopMapMarker
import com.example.gotouchgrass.location.AppLocationTracker
import com.example.gotouchgrass.ui.map.capture.CaptureScreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

private const val CAPTURE_RADIUS_METERS = 100f

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

private data class SelectedPoi(
    val placeId: String, val name: String, val latLng: LatLng
)

private data class SelectedPoiInfo(
    val zoneName: String?
)

private data class CaptureTarget(
    val placeId: String, val placeName: String, val categoryName: String?, val photoBitmap: Bitmap?
)

private data class ResolvedCollectedPlace(
    val placeId: String,
    val name: String,
    val latLng: LatLng?
)

// function generated by GPT-5.3-Codex
private suspend fun fetchCollectedPlaceDetails(
    placesClient: PlacesClient,
    placeId: String
): ResolvedCollectedPlace = suspendCancellableCoroutine { continuation ->
    val fields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
    val request = FetchPlaceRequest.newInstance(placeId, fields)
    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            if (!continuation.isActive) return@addOnSuccessListener
            val place = response.place
            continuation.resume(
                ResolvedCollectedPlace(
                    placeId = placeId,
                    name = place.name ?: placeId,
                    latLng = place.latLng
                )
            )
        }
        .addOnFailureListener {
            if (!continuation.isActive) return@addOnFailureListener
            continuation.resume(
                ResolvedCollectedPlace(
                    placeId = placeId,
                    name = placeId,
                    latLng = null
                )
            )
        }
}

private fun formatCaptureTimestamp(isoTimestamp: String?): String? {
    return isoTimestamp?.let {
        try {
            val offsetDateTime = OffsetDateTime.parse(it)
            val localDateTime = offsetDateTime.toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
            localDateTime.format(formatter)
        } catch (e: Exception) {
            null
        }
    }
}

private fun contestCooldownRemainingMinutes(lastCaptureIso: String?): Long {
    if (lastCaptureIso.isNullOrBlank()) return 0L
    val lastCaptureAt = runCatching { OffsetDateTime.parse(lastCaptureIso) }.getOrNull() ?: return 0L
    val cooldownUntil = lastCaptureAt.plusMinutes(GoTouchGrassRepository.CONTEST_COOLDOWN_MINUTES)
    val remainingSeconds = java.time.Duration.between(OffsetDateTime.now(), cooldownUntil).seconds
    if (remainingSeconds <= 0L) return 0L
    return (remainingSeconds + 59L) / 60L
}

@Composable
fun MapScreen(
    selectedPlaceId: String? = null,
    openCollectedOverlayOnLaunch: Boolean = false,
    placesClient: PlacesClient? = null,
    repository: GoTouchGrassRepository? = null,
    currentUserId: String? = null,
    viewModel: MapViewModel? = null,
    tripViewModel: TripViewModel? = null,
    locationServicesEnabled: Boolean = true,
    locationTracker: AppLocationTracker,
    onCollectedOverlayOpened: () -> Unit = {},
    onPlaceLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLocationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    val userLocation by locationTracker.currentLocation.collectAsState()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isLocationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true || hasLocationPermission(
                context
            )

        if (!isLocationPermissionGranted) {
            locationTracker.stopTracking()
        } else if (locationServicesEnabled) {
            locationTracker.startTracking()
        }
    }

    val mayUseLocation = locationServicesEnabled && isLocationPermissionGranted
    val effectiveUserLocation = if (mayUseLocation) userLocation else null

    // ── Trip UI state ─────────────────────────────────────────────────────────

    // Forward location updates into TripViewModel
    LaunchedEffect(effectiveUserLocation) {
        val loc = effectiveUserLocation ?: return@LaunchedEffect
        tripViewModel?.onLocationUpdate(loc)
    }

    // Resolve route stop place IDs → lat/lngs via Places API
    val pendingStopPlaceIds = tripViewModel?._pendingRouteStopPlaceIds ?: emptyList()
    LaunchedEffect(pendingStopPlaceIds, placesClient) {
        if (pendingStopPlaceIds.isEmpty() || placesClient == null) return@LaunchedEffect
        tripViewModel?.clearPendingRouteStops()

        val resolved = mutableListOf<RouteStopMapMarker>()
        pendingStopPlaceIds.forEachIndexed { idx, (landmarkId, placeId) ->
            val fields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(placeId, fields)
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng ?: return@addOnSuccessListener
                resolved.add(
                    RouteStopMapMarker(
                        stopIndex = idx,
                        landmarkId = landmarkId,
                        placeId = placeId,
                        placeName = place.name ?: "Stop ${idx + 1}",
                        latLng = latLng,
                        hintText = null
                    )
                )
                if (resolved.size == pendingStopPlaceIds.size) {
                    tripViewModel?.updateRouteStopMarkers(resolved.sortedBy { it.stopIndex })
                }
            }
        }
    }

    LaunchedEffect(locationServicesEnabled) {
        if (!locationServicesEnabled) {
            locationTracker.stopTracking()
            return@LaunchedEffect
        }
        if (!isLocationPermissionGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            locationTracker.startTracking()
        }
    }

    var captureTarget by remember { mutableStateOf<CaptureTarget?>(null) }
    var selectedPoi by remember { mutableStateOf<SelectedPoi?>(null) }
    var capturedPlaceIds by remember { mutableStateOf(setOf<String>()) }
    var currentNearbyIndex by remember { mutableIntStateOf(0) }
    var mappedInDbForSelectedPoi by remember { mutableStateOf<Boolean?>(null) }
    var mappedCategoryForSelectedPoi by remember { mutableStateOf<String?>(null) }
    var isResolvingMapping by remember { mutableStateOf(false) }
    var isMappingLandmark by remember { mutableStateOf(false) }
    var mappingLandmarkError by remember { mutableStateOf<String?>(null) }
    var isResolvingPoiInfo by remember { mutableStateOf(false) }
    var selectedPoiPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var captureTimestamp by remember { mutableStateOf<String?>(null) }
    var landmarkOwnershipSummary by remember { mutableStateOf<LandmarkOwnershipSummary?>(null) }
    var leaderboardPlaceId by remember { mutableStateOf<String?>(null) }
    var leaderboardPlaceName by remember { mutableStateOf<String?>(null) }
    var landmarkLeaderboardEntries by remember { mutableStateOf<List<LandmarkLeaderboardEntry>>(emptyList()) }
    var isLoadingLandmarkLeaderboard by remember { mutableStateOf(false) }
    var landmarkLeaderboardError by remember { mutableStateOf<String?>(null) }
    var showCollectedOverlay by remember { mutableStateOf(false) }
    var collectedLandmarks by remember { mutableStateOf<List<CollectedLandmark>>(emptyList()) }
    var isLoadingCollectedLandmarks by remember { mutableStateOf(false) }
    var collectedLandmarksError by remember { mutableStateOf<String?>(null) }
    var resolvedCollectedPlaces by remember {
        mutableStateOf<Map<String, ResolvedCollectedPlace>>(
            emptyMap()
        )
    }
    var selectedCollectedPlaceId by remember { mutableStateOf<String?>(null) }
    val collectedListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isCollectedMode = showCollectedOverlay
    val showCollectedMarkers = isCollectedMode && resolvedCollectedPlaces.isNotEmpty()
    val showNearbyRoutes = !isCollectedMode
    val showCollectedList = isCollectedMode
    val collectedToggleLabel = if (isCollectedMode) {
        "Hide Collected"
    } else {
        "Collected (${collectedLandmarks.size})"
    }

    val savedCameraTarget = viewModel?.savedCameraTarget
    val savedCameraZoom = viewModel?.savedCameraZoom
    val initialCameraTarget = savedCameraTarget ?: effectiveUserLocation ?: LatLng(0.0, 0.0)
    val initialCameraZoom = savedCameraZoom ?: if (effectiveUserLocation != null) 16f else 2f

    // idea and code to load captured place ids generated by Claude Haiku 4.5
    LaunchedEffect(repository, currentUserId) {
        if (repository == null || currentUserId == null) {
            capturedPlaceIds = emptySet()
            return@LaunchedEffect
        }

        val result = repository.getCapturedPlaceIdsByUserId(currentUserId)
        capturedPlaceIds = result.getOrNull() ?: emptySet()
    }

    LaunchedEffect(repository, currentUserId, capturedPlaceIds) {
        if (repository == null || currentUserId == null) {
            collectedLandmarks = emptyList()
            resolvedCollectedPlaces = emptyMap()
            selectedCollectedPlaceId = null
            isLoadingCollectedLandmarks = false
            collectedLandmarksError = null
            return@LaunchedEffect
        }

        isLoadingCollectedLandmarks = true
        collectedLandmarksError = null
        repository.getCollectedLandmarks(currentUserId)
            .onSuccess { items ->
                collectedLandmarks = items
                selectedCollectedPlaceId = items.firstOrNull()?.placeId
            }
            .onFailure { error ->
                collectedLandmarks = emptyList()
                selectedCollectedPlaceId = null
                collectedLandmarksError = error.message ?: "Unable to load captured landmarks"
            }
        isLoadingCollectedLandmarks = false
    }

    LaunchedEffect(openCollectedOverlayOnLaunch) {
        if (!openCollectedOverlayOnLaunch) return@LaunchedEffect
        showCollectedOverlay = true
        onCollectedOverlayOpened()
    }

    LaunchedEffect(tripViewModel?.pendingChallengeSnackbarMessage) {
        val message = tripViewModel?.pendingChallengeSnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        tripViewModel.consumeChallengeSnackbarMessage()
    }

    LaunchedEffect(placesClient, collectedLandmarks) {
        if (placesClient == null || collectedLandmarks.isEmpty()) {
            resolvedCollectedPlaces = emptyMap()
            return@LaunchedEffect
        }

        val resolved = mutableMapOf<String, ResolvedCollectedPlace>()
        collectedLandmarks.forEach { landmark ->
            val details = fetchCollectedPlaceDetails(placesClient, landmark.placeId)
            resolved[landmark.placeId] = details
            resolvedCollectedPlaces = resolved.toMap()
        }
    }

    LaunchedEffect(selectedPlaceId, placesClient) {
        if (selectedPlaceId != null && placesClient != null && selectedPoi == null) {
            val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(selectedPlaceId, placeFields)

            // logic generated by Claude Haiku 4.5
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                if (latLng != null) {
                    mappedInDbForSelectedPoi = null
                    mappedCategoryForSelectedPoi = null
                    isResolvingMapping = repository != null
                    isMappingLandmark = false
                    mappingLandmarkError = null
                    selectedPoiPhoto = null
                    isResolvingPoiInfo = true
                    captureTimestamp = null
                    selectedPoi = SelectedPoi(
                        placeId = selectedPlaceId, name = place.name ?: "", latLng = latLng
                    )
                    onPlaceLoaded()
                }
            }
        }
    }

    LaunchedEffect(selectedPoi?.placeId, placesClient) {
        val poi = selectedPoi
        if (poi == null || placesClient == null) {
            selectedPoiPhoto = null
            isResolvingPoiInfo = false
            return@LaunchedEffect
        }

        isResolvingPoiInfo = true
        // fetch photos from Places API
        val detailFields = listOf(Place.Field.PHOTO_METADATAS)
        val detailRequest = FetchPlaceRequest.newInstance(poi.placeId, detailFields)
        val expectedPlaceId = poi.placeId

        placesClient.fetchPlace(detailRequest).addOnSuccessListener { response ->
            if (selectedPoi?.placeId != expectedPlaceId) return@addOnSuccessListener
            val place = response.place
            isResolvingPoiInfo = false

            val photoMetadata = place.photoMetadatas?.firstOrNull()
            if (photoMetadata == null) {
                selectedPoiPhoto = null
                return@addOnSuccessListener
            }

            val photoRequest =
                FetchPhotoRequest.builder(photoMetadata).setMaxWidth(1000).setMaxHeight(700)
                    .build()

            placesClient.fetchPhoto(photoRequest).addOnSuccessListener { photoResponse ->
                if (selectedPoi?.placeId != expectedPlaceId) return@addOnSuccessListener
                selectedPoiPhoto = photoResponse.bitmap
            }.addOnFailureListener {
                if (selectedPoi?.placeId != expectedPlaceId) return@addOnFailureListener
                selectedPoiPhoto = null
            }
        }.addOnFailureListener {
            if (selectedPoi?.placeId != expectedPlaceId) return@addOnFailureListener
            selectedPoiPhoto = null
            isResolvingPoiInfo = false
        }
    }

    LaunchedEffect(selectedPoi?.placeId, repository) {
        val poi = selectedPoi
        if (poi == null) {
            mappedInDbForSelectedPoi = null
            mappedCategoryForSelectedPoi = null
            isResolvingMapping = false
            isMappingLandmark = false
            mappingLandmarkError = null
            return@LaunchedEffect
        }

        if (repository == null) {
            mappedInDbForSelectedPoi = null
            mappedCategoryForSelectedPoi = null
            isResolvingMapping = false
            isMappingLandmark = false
            mappingLandmarkError = null
            return@LaunchedEffect
        }

        isResolvingMapping = true
        // is this poi mapped
        val mappedResult = repository.isPlaceMappedForCapture(poi.placeId)
        val isMapped = mappedResult.getOrNull()
        val error = mappedResult.exceptionOrNull()

        if (error is CancellationException) {
            mappedInDbForSelectedPoi = null
            mappedCategoryForSelectedPoi = null
            isResolvingMapping = false
            return@LaunchedEffect
        }

        if (mappedResult.isSuccess && isMapped == true) {
            mappedCategoryForSelectedPoi =
                repository.getMappedLandmarkCategoryForPlaceId(poi.placeId).getOrNull()
            mappedInDbForSelectedPoi = true
        } else {
            mappedCategoryForSelectedPoi = null
            mappedInDbForSelectedPoi = false
        }
        isResolvingMapping = false
        if (mappedInDbForSelectedPoi == true) {
            mappingLandmarkError = null
        }
    }

    LaunchedEffect(selectedPoi?.placeId, repository, currentUserId) {
        val poi = selectedPoi
        if (poi == null || repository == null || currentUserId == null || !capturedPlaceIds.contains(
                poi.placeId
            )
        ) {
            captureTimestamp = null
            return@LaunchedEffect
        }

        val result = repository.getLatestCaptureDateForPlaceId(currentUserId, poi.placeId)
        captureTimestamp = result.getOrNull()
    }

    LaunchedEffect(selectedPoi?.placeId, repository) {
        val poi = selectedPoi
        if (poi == null || repository == null) {
            landmarkOwnershipSummary = null
            return@LaunchedEffect
        }

        landmarkOwnershipSummary = repository.getLandmarkOwnershipSummaryByPlaceId(poi.placeId)
            .getOrNull()
    }

    LaunchedEffect(leaderboardPlaceId, repository) {
        val placeId = leaderboardPlaceId
        if (placeId == null || repository == null) {
            landmarkLeaderboardEntries = emptyList()
            landmarkLeaderboardError = null
            isLoadingLandmarkLeaderboard = false
            return@LaunchedEffect
        }

        isLoadingLandmarkLeaderboard = true
        landmarkLeaderboardError = null
        repository.getLandmarkLeaderboardByPlaceId(placeId)
            .onSuccess { rows ->
                landmarkLeaderboardEntries = rows
            }
            .onFailure { error ->
                landmarkLeaderboardEntries = emptyList()
                landmarkLeaderboardError = error.message ?: "Unable to load leaderboard"
            }
        isLoadingLandmarkLeaderboard = false
    }

    captureTarget?.let { target ->
        CaptureScreen(
            placeId = target.placeId,
            placeName = target.placeName,
            categoryName = target.categoryName,
            placePhotoBitmap = target.photoBitmap,
            repository = repository,
            currentUserId = currentUserId,
            onClose = { captureTarget = null },
            onCaptured = { capturedPlaceId, completedChallengeTitles ->
                capturedPlaceIds = capturedPlaceIds + capturedPlaceId
                selectedPoi = null
                captureTarget = null
                // 120 XP is the fixed capture award (matches recordCaptureByPlaceId)
                tripViewModel?.onCapture(xpAwarded = 120)
                if (completedChallengeTitles.isNotEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Challenge completed: ${completedChallengeTitles.joinToString(", ")}"
                        )
                    }
                }
                viewModel?.refresh()
            })
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, initialCameraZoom)
    }

    LaunchedEffect(
        isCollectedMode,
        collectedLandmarks,
        resolvedCollectedPlaces,
        cameraPositionState
    ) {
        if (!isCollectedMode || collectedLandmarks.isEmpty()) return@LaunchedEffect

        snapshotFlow { collectedListState.firstVisibleItemIndex }
            .collect { index ->
                val item = collectedLandmarks.getOrNull(index) ?: return@collect
                if (selectedCollectedPlaceId != item.placeId) {
                    selectedCollectedPlaceId = item.placeId
                }
                val targetLatLng = resolvedCollectedPlaces[item.placeId]?.latLng ?: return@collect
                val currentPosition = cameraPositionState.position
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(currentPosition).target(targetLatLng).build()
                    ),
                    durationMs = 500
                )
            }
    }

    var lastCenteredUserLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(effectiveUserLocation, selectedPoi?.placeId) {
        if (selectedPoi != null) return@LaunchedEffect
        val location = effectiveUserLocation ?: return@LaunchedEffect

        val movedEnough = lastCenteredUserLocation?.let { previous ->
            val distanceResult = FloatArray(1)
            Location.distanceBetween(
                previous.latitude,
                previous.longitude,
                location.latitude,
                location.longitude,
                distanceResult
            )
            distanceResult[0] >= 3f
        } ?: true

        if (!movedEnough) return@LaunchedEffect

        val cameraUpdate = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder().target(location).zoom(16f).build()
        )

        if (viewModel?.hasFocusedOnUserLocation == true) {
            cameraPositionState.move(cameraUpdate)
        } else {
            cameraPositionState.animate(
                update = cameraUpdate, durationMs = 900
            )
            viewModel?.markUserLocationFocused()
        }

        lastCenteredUserLocation = location
    }

    DisposableEffect(viewModel, cameraPositionState) {
        onDispose {
            val position = cameraPositionState.position
            viewModel?.saveCameraPosition(position.target, position.zoom)
        }
    }

    // move camera to selected location
    LaunchedEffect(selectedPoi) {
        selectedPoi?.let { poi ->
            val currentPosition = cameraPositionState.position
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder(currentPosition).target(poi.latLng).build()
                ), durationMs = 1000
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        val focusCameraOn: (LatLng, Float?) -> Unit = { targetLatLng, minZoom ->
            coroutineScope.launch {
                val currentPosition = cameraPositionState.position
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(currentPosition)
                            .target(targetLatLng)
                            .apply {
                                if (minZoom != null) {
                                    zoom(maxOf(currentPosition.zoom, minZoom))
                                }
                            }
                            .build()
                    ),
                    durationMs = 700
                )
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = mayUseLocation),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = true,
            ),
            contentPadding = PaddingValues(top = 100.dp, bottom = 104.dp),
            onPOIClick = { poi ->
                mappedInDbForSelectedPoi = null
                mappedCategoryForSelectedPoi = null
                isResolvingMapping = repository != null
                isMappingLandmark = false
                mappingLandmarkError = null
                selectedPoiPhoto = null
                isResolvingPoiInfo = placesClient != null
                captureTimestamp = null
                selectedPoi = SelectedPoi(
                    placeId = poi.placeId, name = poi.name, latLng = poi.latLng
                )
            }) {
            effectiveUserLocation?.let { location ->
                Circle(
                    center = location,
                    radius = CAPTURE_RADIUS_METERS.toDouble(),
                    fillColor = Color(0x2234A853),
                    strokeColor = Color(0xCC34A853),
                    strokeWidth = 3f,
                    clickable = false,
                    zIndex = -1f
                )
            }

            // add marker at selected location
            selectedPoi?.let { poi ->
                Marker(
                    state = MarkerState(position = poi.latLng), title = poi.name
                )
            }

            if (showCollectedMarkers) {
                resolvedCollectedPlaces.values.forEach { place ->
                    val latLng = place.latLng ?: return@forEach
                    val isSelected = place.placeId == selectedCollectedPlaceId
                    Marker(
                        state = MarkerState(position = latLng),
                        title = if (isSelected) "${place.name} (selected)" else place.name,
                        snippet = "Captured landmark",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isSelected) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED
                        ),
                        onClick = {
                            selectedCollectedPlaceId = place.placeId
                            val itemIndex =
                                collectedLandmarks.indexOfFirst { it.placeId == place.placeId }
                            if (itemIndex >= 0) {
                                coroutineScope.launch {
                                    collectedListState.animateScrollToItem(itemIndex)
                                }
                            }
                            focusCameraOn(latLng, 16f)
                            true
                        }
                    )
                }
            }

            // Friend approximate location markers
            viewModel?.friendLocations?.forEach { friend ->
                Marker(
                    state = MarkerState(position = friend.latLng),
                    title = friend.displayName,
                    snippet = "Approx. location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                )
            }

            // Route stop markers when a route trip is active
            tripViewModel?.routeStopMarkers?.forEachIndexed { idx, stop ->
                val isCaptured = capturedPlaceIds.contains(stop.placeId)
                Marker(
                    state = MarkerState(position = stop.latLng),
                    title = stop.placeName,
                    snippet = stop.hintText ?: "Stop ${idx + 1}",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (isCaptured) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
        }

        // --- Overlay: Header (compact centered pill) ---
        viewModel?.let { vm ->
            val header = vm.headerStats
            val streak = maxOf(header.streakDays, tripViewModel?.streakDays ?: 0)
            MapHeaderOverlay(
                level = header.level,
                currentXp = header.currentXp,
                maxXp = header.maxXp,
                xpToNext = header.xpToNextLevel,
                streakDays = streak,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = 12.dp,
                        start = GoTouchGrassDimens.SpacingMd,
                        end = GoTouchGrassDimens.SpacingMd
                    )
            )
        }

        // --- Overlay: Active trip bar (shown when trip is running) ---
        tripViewModel?.let { tvm ->
            if (tvm.isActive) {
                ActiveTripBar(
                    elapsedSeconds = tvm.elapsedSeconds,
                    distanceMeters = tvm.distanceMeters,
                    xpEarned = tvm.xpEarned,
                    captureCount = tvm.captureCount,
                    routeName = tvm.activeRouteName,
                    onEndTrip = { tvm.endTrip(); viewModel?.refresh() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = GoTouchGrassDimens.SpacingMd,
                            end = GoTouchGrassDimens.SpacingMd,
                            bottom = 72.dp
                        )
                )
            }
        }

        // --- Overlay: Footer — route card + Free Roam in one surface (hidden during active trip) ---
        val tripActive = tripViewModel?.isActive == true
        if (!tripActive && showNearbyRoutes) {
            val routes = viewModel?.nearbyRoutes ?: emptyList()
            if (currentNearbyIndex !in routes.indices && routes.isNotEmpty()) {
                currentNearbyIndex = 0
            }
            NearbyAreasOverlay(
                route = routes.getOrNull(currentNearbyIndex),
                index = currentNearbyIndex,
                total = routes.size,
                showTripLauncher = tripViewModel != null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = GoTouchGrassDimens.SpacingMd,
                        end = 96.dp,
                        bottom = 72.dp
                    ),
                onNext = {
                    if (routes.isNotEmpty()) currentNearbyIndex =
                        (currentNearbyIndex + 1) % routes.size
                },
                onStartRoute = { route -> tripViewModel?.startRouteTrip(route) },
                onStartFreeRoam = { tripViewModel?.startFreeRoamTrip() }
            )
        }

        Button(
            onClick = { showCollectedOverlay = !showCollectedOverlay },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 74.dp,
                    end = GoTouchGrassDimens.SpacingMd
                )
        ) {
            Text(collectedToggleLabel)
        }

        if (showCollectedList) {
            CollectedLandmarksOverlay(
                items = collectedLandmarks,
                resolvedByPlaceId = resolvedCollectedPlaces,
                selectedPlaceId = selectedCollectedPlaceId,
                isLoading = isLoadingCollectedLandmarks,
                errorMessage = collectedLandmarksError,
                listState = collectedListState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = GoTouchGrassDimens.SpacingMd,
                        end = GoTouchGrassDimens.SpacingMd,
                        bottom = 12.dp
                    ),
                onSelect = { placeId ->
                    selectedCollectedPlaceId = placeId
                    val place = resolvedCollectedPlaces[placeId]
                    val latLng = place?.latLng
                    if (latLng != null) {
                        focusCameraOn(latLng, 16f)
                    }
                }
            )
        }

        selectedPoi?.let { poi ->
            val distanceResult = FloatArray(1)
            val distanceMeters = effectiveUserLocation?.let { location ->
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    poi.latLng.latitude,
                    poi.latLng.longitude,
                    distanceResult
                )
                distanceResult[0]
            }
            val isNearby = distanceMeters != null && distanceMeters <= CAPTURE_RADIUS_METERS
            val isCaptured = capturedPlaceIds.contains(poi.placeId)
            val contestCooldownMinutes = if (isCaptured) {
                contestCooldownRemainingMinutes(captureTimestamp)
            } else {
                0L
            }
            CapturePoiPopup(
                selectedPoi = poi,
                isResolvingPoiInfo = isResolvingPoiInfo,
                isMapped = mappedInDbForSelectedPoi,
                isResolvingMapping = isResolvingMapping,
                isMappingLandmark = isMappingLandmark,
                isNearby = isNearby,
                distanceMeters = distanceMeters,
                isCaptured = isCaptured,
                captureTimestamp = captureTimestamp,
                firstDiscovererName = landmarkOwnershipSummary?.firstDiscovererName,
                currentLeaderName = landmarkOwnershipSummary?.currentLeaderName,
                currentLeaderContestScore = landmarkOwnershipSummary?.currentLeaderContestScore ?: 0,
                currentLeaderLastContestAtIso = landmarkOwnershipSummary?.currentLeaderLastContestAtIso,
                mostRecentCapturerName = landmarkOwnershipSummary?.mostRecentCapturerName,
                mostRecentCaptureTimestamp = landmarkOwnershipSummary?.mostRecentCaptureAtIso,
                contestCooldownMinutes = contestCooldownMinutes,
                mappingErrorMessage = mappingLandmarkError,
                onViewLeaderboard = {
                    leaderboardPlaceId = poi.placeId
                    leaderboardPlaceName = poi.name
                    selectedPoi = null
                },
                onMapLandmark = {
                    if (repository == null || currentUserId == null || isMappingLandmark) return@CapturePoiPopup
                    val selectedPlaceIdForRequest = poi.placeId
                    isMappingLandmark = true
                    mappingLandmarkError = null
                    coroutineScope.launch {
                        val mappingResult =
                            repository.ensureLandmarkMappedForCapture(
                                userId = currentUserId,
                                placeId = selectedPlaceIdForRequest
                            )
                        if (selectedPoi?.placeId == selectedPlaceIdForRequest) {
                            if (mappingResult.isSuccess) {
                                mappedCategoryForSelectedPoi = mappingResult.getOrNull()
                                mappedInDbForSelectedPoi = true
                                mappingLandmarkError = null
                            } else {
                                mappingLandmarkError =
                                    mappingResult.exceptionOrNull()?.message
                                        ?: "Could not map this landmark right now. Try again."
                            }
                        }
                        isMappingLandmark = false
                    }
                },
                onCapture = {
                    val alreadyCaptured = capturedPlaceIds.contains(poi.placeId)
                    if (mappedInDbForSelectedPoi != true) return@CapturePoiPopup
                    if (repository == null || currentUserId == null) return@CapturePoiPopup

                    if (alreadyCaptured) {
                        if (contestCooldownMinutes > 0L) return@CapturePoiPopup
                        coroutineScope.launch {
                            val contestResult = repository.recordCaptureByPlaceId(currentUserId, poi.placeId)
                            if (contestResult.isSuccess) {
                                capturedPlaceIds = capturedPlaceIds + poi.placeId
                                tripViewModel?.onCapture(xpAwarded = 30)
                                captureTimestamp = repository.getLatestCaptureDateForPlaceId(
                                    currentUserId,
                                    poi.placeId
                                ).getOrNull()
                                landmarkOwnershipSummary = repository
                                    .getLandmarkOwnershipSummaryByPlaceId(poi.placeId)
                                    .getOrNull()
                                mappingLandmarkError = null
                                viewModel?.refresh()
                            } else {
                                mappingLandmarkError =
                                    contestResult.exceptionOrNull()?.message
                                        ?: "Contest failed. Try again shortly."
                            }
                        }
                        return@CapturePoiPopup
                    }

                    coroutineScope.launch {
                        val mappingResult = repository.ensureLandmarkMappedForCapture(
                            userId = currentUserId,
                            placeId = poi.placeId
                        )
                        if (mappingResult.isSuccess) {
                            mappedCategoryForSelectedPoi = mappingResult.getOrNull()
                            captureTarget = CaptureTarget(
                                placeId = poi.placeId,
                                placeName = poi.name,
                                categoryName = mappedCategoryForSelectedPoi,
                                photoBitmap = selectedPoiPhoto
                            )
                        } else {
                            mappingLandmarkError =
                                mappingResult.exceptionOrNull()?.message
                                    ?: "This landmark could not be prepared for capture."
                        }
                    }
                },
                onClose = { selectedPoi = null })
        }

        if (leaderboardPlaceId != null) {
            LandmarkLeaderboardOverlay(
                placeName = leaderboardPlaceName ?: "Location Leaderboard",
                entries = landmarkLeaderboardEntries,
                currentUserId = currentUserId,
                isLoading = isLoadingLandmarkLeaderboard,
                errorMessage = landmarkLeaderboardError,
                onClose = {
                    leaderboardPlaceId = null
                    leaderboardPlaceName = null
                }
            )
        }

        // --- Trip Celebration Overlay (casino-style XP reveal) ---
        tripViewModel?.let { tvm ->
            if (tvm.showCelebration) {
                val summary = tvm.lastSummary
                if (summary != null) {
                    TripCelebrationOverlay(
                        summary = summary,
                        onDismiss = { tvm.dismissCelebration() }
                    )
                }
            }
        }

        // --- Trip Summary Dialog ---
        tripViewModel?.let { tvm ->
            if (tvm.showSummary) {
                val summary = tvm.lastSummary
                if (summary != null) {
                    TripSummaryDialog(
                        summary = summary,
                        onDismiss = {
                            tvm.dismissSummary()
                            viewModel?.refresh()
                        }
                    )
                }
            }
        }

        // --- Level-Up Dialog ---
        tripViewModel?.let { tvm ->
            if (tvm.levelUp) {
                LevelUpDialog(
                    newLevel = tvm.newLevel,
                    onDismiss = { tvm.dismissLevelUp(); viewModel?.refresh() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        if (!locationServicesEnabled || !isLocationPermissionGranted) {
            val message = if (!locationServicesEnabled) {
                "Turn on Location Services in account settings to use the map."
            } else {
                "Location permission is required to use the map."
            }
            LocationRequiredOverlay(
                message = message,
                showPermissionAction = !isLocationPermissionGranted,
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                })
        }
    }
}

@Composable
private fun CollectedLandmarksOverlay(
    items: List<CollectedLandmark>,
    resolvedByPlaceId: Map<String, ResolvedCollectedPlace>,
    selectedPlaceId: String?,
    isLoading: Boolean,
    errorMessage: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)),
        tonalElevation = GoTouchGrassDimens.ElevationNone
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Collected Landmarks (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            when {
                isLoading -> {
                    Text(
                        text = "Loading your captured landmarks...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                !errorMessage.isNullOrBlank() -> {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                items.isEmpty() -> {
                    Text(
                        text = "No landmarks captured yet. Capture a mapped location to see it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val maxHeight = 240.dp
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxHeight),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(items, key = { _, item -> item.placeId }) { _, item ->
                                val place = resolvedByPlaceId[item.placeId]
                                val isSelected = selectedPlaceId == item.placeId
                                val rowColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(item.placeId) },
                                    colors = CardDefaults.cardColors(containerColor = rowColor)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = place?.name ?: item.placeId,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Captured ${formatCaptureTimestamp(item.capturedAtIso) ?: item.capturedAtIso}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (place?.latLng == null) {
                                            Text(
                                                text = "Resolving map location...",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationRequiredOverlay(
    message: String, showPermissionAction: Boolean, onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GoTouchGrassDimens.SpacingMd),
            shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoTouchGrassDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current location required", style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = message, style = MaterialTheme.typography.bodyMedium
                )

                if (showPermissionAction) {
                    Button(onClick = onRequestPermission) {
                        Text("Grant Location Access")
                    }
                }
            }
        }
    }
}

@Composable
private fun MapHeaderOverlay(
    level: Int,
    currentXp: Int,
    maxXp: Int,
    xpToNext: Int,
    modifier: Modifier = Modifier,
    streakDays: Int = 0
) {
    val cardSurface = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    val border = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardSurface,
        border = BorderStroke(0.5.dp, border),
        tonalElevation = GoTouchGrassDimens.ElevationNone
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent), contentAlignment = Alignment.Center
                    ) {
                        Text(text = level.toString(), fontSize = 10.sp, color = onAccent)
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Level $level Explorer",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = onSurface
                    )
                }

                // Streak flame badge
                if (streakDays > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "🔥", fontSize = 13.sp)
                        Text(
                            text = "$streakDays",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE8B931)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = {
                        if (maxXp <= 0) 0f else (currentXp.toFloat() / maxXp.toFloat()).coerceIn(
                            0f, 1f
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = onSurface.copy(alpha = 0.08f),
                    drawStopIndicator = {})

                Text(
                    text = "$xpToNext XP to next",
                    style = MaterialTheme.typography.labelSmall,
                    color = mutedText
                )
            }
        }
    }
}

@Composable
private fun NearbyAreasOverlay(
    route: com.example.gotouchgrass.domain.ExploreRouteItem?,
    index: Int,
    total: Int,
    modifier: Modifier = Modifier,
    showTripLauncher: Boolean = false,
    onNext: () -> Unit,
    onStartRoute: ((com.example.gotouchgrass.domain.ExploreRouteItem) -> Unit)? = null,
    onStartFreeRoam: (() -> Unit)? = null
) {
    // Nothing to show at all
    if (route == null && !showTripLauncher) return

    val cardSurface = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary

    Surface(
        modifier = modifier
            .fillMaxWidth(0.82f)
            .widthIn(max = 440.dp),
        shape = RoundedCornerShape(14.dp),
        color = cardSurface,
        border = BorderStroke(0.5.dp, onSurface.copy(alpha = 0.09f)),
        tonalElevation = GoTouchGrassDimens.ElevationNone
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Route section (only if a route exists) ────────────────────────
            if (route != null) {
                Column(
                    modifier = Modifier
                        .clickable { onNext() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${
                                    route.theme.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                } · nearby",
                                fontSize = 10.sp, color = muted
                            )
                            Text(
                                text = route.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurface,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${route.zoneCount} stops · ~${route.hours}h",
                                fontSize = 11.sp,
                                color = muted,
                                maxLines = 1
                            )
                        }

                        if (showTripLauncher) {
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onStartRoute?.invoke(route) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent, contentColor = onAccent
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Start Route", fontSize = 11.sp, color = onAccent)
                            }
                        }
                    }

                    if (total > 1) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                5.dp, Alignment.CenterHorizontally
                            )
                        ) {
                            repeat(total) { i ->
                                val active = i == index
                                Box(
                                    modifier = Modifier
                                        .size(width = if (active) 18.dp else 6.dp, height = 4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (active) accent else onSurface.copy(alpha = 0.15f)
                                        )
                                        .clickable { onNext() }
                                )
                            }
                        }
                    }
                }
            }

            // ── Free Roam button ─────────────────────────────────────────────
            if (showTripLauncher) {
                if (route != null) {
                    HorizontalDivider(
                        color = onSurface.copy(alpha = 0.07f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
                OutlinedButton(
                    onClick = { onStartFreeRoam?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (route != null) "Start Free Roam Instead" else "Start Free Roam",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun CapturePoiPopup(
    selectedPoi: SelectedPoi,
    isResolvingPoiInfo: Boolean,
    isMapped: Boolean?,
    isResolvingMapping: Boolean,
    isMappingLandmark: Boolean,
    isNearby: Boolean,
    distanceMeters: Float?,
    isCaptured: Boolean,
    captureTimestamp: String?,
    firstDiscovererName: String?,
    currentLeaderName: String?,
    currentLeaderContestScore: Int,
    currentLeaderLastContestAtIso: String?,
    mostRecentCapturerName: String?,
    mostRecentCaptureTimestamp: String?,
    contestCooldownMinutes: Long,
    mappingErrorMessage: String?,
    onViewLeaderboard: () -> Unit,
    onMapLandmark: () -> Unit,
    onCapture: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            .clickable { onClose() }) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(GoTouchGrassDimens.SpacingMd),
            shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoTouchGrassDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Text(
                    text = selectedPoi.name, style = MaterialTheme.typography.titleMedium
                )

                if (isResolvingPoiInfo) {
                    Text(
                        text = "Loading place details...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = when {
                        isResolvingPoiInfo || isResolvingMapping || isMapped == null -> "Loading location..."
                        !isMapped -> "You are the first explorer here! Map this landmark once, then capture it."
                        distanceMeters == null -> "Location unavailable."
                        isNearby -> "Nearby (${distanceMeters.toInt()}m)"
                        else -> "Move closer (${distanceMeters.toInt()}m)"
                    }, style = MaterialTheme.typography.bodySmall
                )

                if (!mappingErrorMessage.isNullOrBlank()) {
                    Text(
                        text = mappingErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isMapped == true) {
                    val firstDiscovererLabel = firstDiscovererName ?: "Unknown"
                    Text(
                        text = "First discovered by $firstDiscovererLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val leaderLabel = currentLeaderName ?: "No leader yet"
                    Text(
                        text = "Current leader: $leaderLabel ($currentLeaderContestScore pts)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val mostRecentLabel = mostRecentCapturerName ?: "No captures yet"
                    val formattedMostRecentTime = formatCaptureTimestamp(mostRecentCaptureTimestamp)
                    val mostRecentDetails = if (formattedMostRecentTime != null && mostRecentCapturerName != null) {
                        "$mostRecentLabel at $formattedMostRecentTime"
                    } else {
                        mostRecentLabel
                    }
                }

                // Show capture timestamp if already captured
                if (isCaptured && captureTimestamp != null) {
                    Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))
                    val formattedTime = formatCaptureTimestamp(captureTimestamp)
                    if (formattedTime != null) {
                        Text(
                            text = "You captured this landmark $formattedTime",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                ) {
                    val shouldShowMapLandmarkAction = isMapped == false
                    Button(
                        onClick = {
                            if (shouldShowMapLandmarkAction) {
                                onMapLandmark()
                            } else {
                                onCapture()
                            }
                        },
                        enabled = if (shouldShowMapLandmarkAction) {
                            !isResolvingPoiInfo && !isResolvingMapping && !isMappingLandmark
                        } else {
                            !isResolvingMapping && !isMappingLandmark && isMapped == true && isNearby && (!isCaptured || contestCooldownMinutes == 0L)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when {
                                isResolvingPoiInfo || isResolvingMapping || isMapped == null -> "Loading"
                                isMappingLandmark -> "Mapping..."
                                !isMapped -> "Map Landmark"
                                isCaptured && contestCooldownMinutes > 0L -> "Cooldown ${contestCooldownMinutes} min"
                                isCaptured -> "Claim Points"
                                isNearby -> "Capture"
                                else -> "Not Nearby"
                            }
                        )
                    }

                    Button(
                        onClick = onClose, modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }

                if (isMapped == true) {
                    OutlinedButton(
                        onClick = onViewLeaderboard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Location Leaderboard")
                    }
                }
            }
        }
    }
}

@Composable
private fun LandmarkLeaderboardOverlay(
    placeName: String,
    entries: List<LandmarkLeaderboardEntry>,
    currentUserId: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            .clickable { onClose() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(GoTouchGrassDimens.SpacingMd),
            shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoTouchGrassDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Text(
                    text = placeName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Location Leaderboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when {
                    isLoading -> Text(
                        text = "Loading leaderboard...",
                        style = MaterialTheme.typography.bodySmall
                    )

                    !errorMessage.isNullOrBlank() -> Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    entries.isEmpty() -> Text(
                        text = "No leaderboard activity yet.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    else -> {
                        entries.forEachIndexed { index, entry ->
                            val isCurrentUser = currentUserId != null && entry.authUserId == currentUserId
                            val displayName = if (isCurrentUser) "YOU" else entry.displayName
                            val lastEvent = formatCaptureTimestamp(entry.lastEventAtIso)
                            val subtitle = if (lastEvent != null) {
                                "${entry.score} pts · last event $lastEvent"
                            } else {
                                "${entry.score} pts"
                            }
                            Text(
                                text = "${index + 1}. $displayName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrentUser) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}


////////////////////////////////////////////////////////////
// PREVIEW
////////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun MapScreenPreview() {
    GoTouchGrassTheme {
        val vm = remember {
            val model = MapModel(
                currentUserId = "user_you",
                profileRepository = FakeProfileRepository(),
                mapRepository = FakeMapRepository()
            )
            MapViewModel(model = model)
        }
        val context = LocalContext.current
        val locationTracker = remember { AppLocationTracker(context.applicationContext) }
        MapScreen(
            viewModel = vm, locationTracker = locationTracker
        )
    }
}
