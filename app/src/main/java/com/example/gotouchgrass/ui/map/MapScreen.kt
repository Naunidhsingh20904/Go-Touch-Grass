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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

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

@Composable
fun MapScreen(
    selectedPlaceId: String? = null,
    placesClient: PlacesClient? = null,
    repository: GoTouchGrassRepository? = null,
    currentUserId: String? = null,
    viewModel: MapViewModel? = null,
    tripViewModel: TripViewModel? = null,
    locationServicesEnabled: Boolean = true,
    locationTracker: AppLocationTracker,
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
    var isResolvingPoiInfo by remember { mutableStateOf(false) }
    var selectedPoiPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var captureTimestamp by remember { mutableStateOf<String?>(null) }

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
            return@LaunchedEffect
        }

        if (repository == null) {
            mappedInDbForSelectedPoi = null
            mappedCategoryForSelectedPoi = null
            isResolvingMapping = false
            return@LaunchedEffect
        }

        isResolvingMapping = true
        // is this poi mapped
        val mappingResult = repository.getMappedLandmarkCategoryForPlaceId(poi.placeId)
        val category = mappingResult.getOrNull()
        val error = mappingResult.exceptionOrNull()

        if (error is CancellationException) {
            mappedInDbForSelectedPoi = null
            mappedCategoryForSelectedPoi = null
            isResolvingMapping = false
            return@LaunchedEffect
        }

        mappedCategoryForSelectedPoi = if (mappingResult.isSuccess) category else null
        mappedInDbForSelectedPoi = mappingResult.isSuccess && category != null
        isResolvingMapping = false
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

    captureTarget?.let { target ->
        CaptureScreen(
            placeId = target.placeId,
            placeName = target.placeName,
            categoryName = target.categoryName,
            placePhotoBitmap = target.photoBitmap,
            repository = repository,
            currentUserId = currentUserId,
            onClose = { captureTarget = null },
            onCaptured = { capturedPlaceId ->
                capturedPlaceIds = capturedPlaceIds + capturedPlaceId
                captureTarget = null
                selectedPoi = null
                // 120 XP is the fixed capture award (matches recordCaptureByPlaceId)
                tripViewModel?.onCapture(xpAwarded = 120)
                viewModel?.refresh()
            })
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, initialCameraZoom)
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

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = mayUseLocation),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = true,
            ),
            contentPadding = PaddingValues(top = 100.dp, bottom = 160.dp),
            onPOIClick = { poi ->
                mappedInDbForSelectedPoi = null
                mappedCategoryForSelectedPoi = null
                isResolvingMapping = repository != null
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

            // Route stop markers when a route trip is active
            tripViewModel?.routeStopMarkers?.forEachIndexed { idx, stop ->
                val isCaptured = capturedPlaceIds.contains(stop.placeId)
                Marker(
                    state = MarkerState(position = stop.latLng),
                    title = stop.placeName,
                    snippet = if (stop.hintText != null) stop.hintText else "Stop ${idx + 1}",
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
            MapHeaderOverlay(
                level = header.level,
                currentXp = header.currentXp,
                maxXp = header.maxXp,
                xpToNext = header.xpToNextLevel,
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
        if (!tripActive) {
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
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = GoTouchGrassDimens.SpacingMd,
                        end = GoTouchGrassDimens.SpacingMd,
                        bottom = 72.dp
                    ),
                onNext = {
                    if (routes.isNotEmpty()) currentNearbyIndex = (currentNearbyIndex + 1) % routes.size
                },
                onStartRoute = { route -> tripViewModel?.startRouteTrip(route) },
                onStartFreeRoam = { tripViewModel?.startFreeRoamTrip() }
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
            CapturePoiPopup(
                selectedPoi = poi,
                isResolvingPoiInfo = isResolvingPoiInfo,
                isMapped = mappedInDbForSelectedPoi,
                isResolvingMapping = isResolvingMapping,
                isNearby = isNearby,
                distanceMeters = distanceMeters,
                isCaptured = capturedPlaceIds.contains(poi.placeId),
                captureTimestamp = captureTimestamp,
                onCapture = {
                    val alreadyCaptured = capturedPlaceIds.contains(poi.placeId)
                    if (mappedInDbForSelectedPoi != true || alreadyCaptured) return@CapturePoiPopup
                    captureTarget = CaptureTarget(
                        placeId = poi.placeId,
                        placeName = poi.name,
                        categoryName = mappedCategoryForSelectedPoi,
                        photoBitmap = selectedPoiPhoto
                    )
                },
                onClose = { selectedPoi = null })
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
    level: Int, currentXp: Int, maxXp: Int, xpToNext: Int, modifier: Modifier = Modifier
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
                        Text(
                            text = level.toString(), fontSize = 10.sp, color = onAccent
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Level $level Explorer",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = onSurface
                    )
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
    showTripLauncher: Boolean = false,
    modifier: Modifier = Modifier,
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
        modifier = modifier.fillMaxWidth(),
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
    isNearby: Boolean,
    distanceMeters: Float?,
    isCaptured: Boolean,
    captureTimestamp: String?,
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
                        !isMapped -> "Unmapped landmark. Capture unavailable."
                        distanceMeters == null -> "Location unavailable."
                        isNearby -> "Nearby (${distanceMeters.toInt()}m)"
                        else -> "Move closer (${distanceMeters.toInt()}m)"
                    }, style = MaterialTheme.typography.bodySmall
                )

                // Show capture timestamp if already captured
                if (isCaptured && captureTimestamp != null) {
                    Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))
                    val formattedTime = formatCaptureTimestamp(captureTimestamp)
                    if (formattedTime != null) {
                        Text(
                            text = "Captured: $formattedTime",
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
                    Button(
                        onClick = onCapture,
                        enabled = !isResolvingMapping && isMapped == true && isNearby && !isCaptured,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when {
                                isResolvingPoiInfo || isResolvingMapping || isMapped == null -> "Loading"
                                !isMapped -> "Unavailable"
                                isCaptured -> "Captured"
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
