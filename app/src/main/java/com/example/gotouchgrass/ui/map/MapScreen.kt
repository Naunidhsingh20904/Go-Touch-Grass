package com.example.gotouchgrass.ui.map

import android.Manifest
import android.location.Location
import android.content.pm.PackageManager
import android.os.Looper
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.data.FakeMapRepository
import com.example.gotouchgrass.data.FakeProfileRepository
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.MapModel
import com.example.gotouchgrass.ui.map.capture.CaptureScreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Circle
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import androidx.compose.ui.text.font.FontWeight

private const val CAPTURE_RADIUS_METERS = 100f
private const val DEMO_EGG_FOUNTAIN_ID = "lm_uw_egg_fountain"
private val UW_DEMO_LOCATION = LatLng(43.4723, -80.5449)

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

private data class SelectedPoi(
    val placeId: String,
    val name: String,
    val latLng: LatLng
)

private data class SelectedPoiInfo(
    val zoneName: String?,
    val categoryName: String?,
    val ownerDisplayName: String?
)

private fun selectedPoiInfo(selectedPoi: SelectedPoi): SelectedPoiInfo {
    val matchedLandmark = FakeData.landmarks.firstOrNull { landmark ->
        landmark.id == selectedPoi.placeId || landmark.name.equals(selectedPoi.name, ignoreCase = true)
    }

    val zone = matchedLandmark?.let { landmark ->
        FakeData.zones.firstOrNull { it.id == landmark.zoneId }
    }

    val ownerDisplayName = zone?.let { matchedZone ->
        FakeData.zoneOwnership
            .firstOrNull { it.zoneId == matchedZone.id }
            ?.ownerUserId
            ?.let { ownerId -> FakeData.users.firstOrNull { it.id == ownerId }?.displayName }
    }

    return SelectedPoiInfo(
        zoneName = zone?.name,
        categoryName = matchedLandmark?.category?.name?.replace("_", " "),
        ownerDisplayName = ownerDisplayName
    )
}

private fun resolveCapturePlaceIdForDemo(placeId: String): String {
    val isKnownLandmark = FakeData.landmarks.any { it.id == placeId }
    return if (isKnownLandmark) placeId else DEMO_EGG_FOUNTAIN_ID
}

@Composable
fun MapScreen(
    selectedPlaceId: String? = null,
    placesClient: PlacesClient? = null,
    repository: GoTouchGrassRepository? = null,
    viewModel: MapViewModel? = null,
    locationServicesEnabled: Boolean = true,
    onPlaceLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLocationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isLocationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            hasLocationPermission(context)

        if (!isLocationPermissionGranted) {
            userLocation = null
            locationError = "Location permission is required to use the map."
        }
    }

    val mayUseLocation = locationServicesEnabled && isLocationPermissionGranted

    LaunchedEffect(locationServicesEnabled) {
        if (!locationServicesEnabled) {
            userLocation = null
            locationError = null
            return@LaunchedEffect
        }
        if (!isLocationPermissionGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val latest = result.lastLocation ?: return
                userLocation = LatLng(latest.latitude, latest.longitude)
                locationError = null
            }
        }
    }

    // looper logic generated by GPT-5.3-Codex
    DisposableEffect(mayUseLocation) {
        if (!mayUseLocation) {
            onDispose { }
        } else {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
                .addOnFailureListener {
                    locationError = "Unable to access current location."
                }

            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    var capturePlaceId by remember { mutableStateOf<String?>(null) }
    var selectedPoi by remember { mutableStateOf<SelectedPoi?>(null) }
    var capturedPlaceIds by remember { mutableStateOf(setOf<String>()) }
    var currentNearbyIndex by remember { mutableStateOf(0) }

    LaunchedEffect(selectedPlaceId, placesClient) {
        if (selectedPlaceId != null && placesClient != null && selectedPoi == null) {
            val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(selectedPlaceId, placeFields)
            
            // logic generated by Claude Haiku 4.5
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    val latLng = place.latLng
                    if (latLng != null) {
                        selectedPoi = SelectedPoi(
                            placeId = selectedPlaceId,
                            name = place.name ?: "",
                            latLng = latLng
                        )
                        onPlaceLoaded()
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        // Fallback to FakeData if API fails
                        val landmark = FakeData.landmarks.firstOrNull { it.id == selectedPlaceId }
                        landmark?.let {
                            selectedPoi = SelectedPoi(
                                placeId = it.id,
                                name = it.name,
                                latLng = LatLng(it.latLng.latitude, it.latLng.longitude)
                            )
                            onPlaceLoaded()
                        }
                    }
                }
        }
    }

    capturePlaceId?.let { placeId ->
        CaptureScreen(
            placeId = placeId,
            onClose = { capturePlaceId = null },
            onCaptured = { capturedPlaceId ->
                capturedPlaceIds = capturedPlaceIds + capturedPlaceId
                capturePlaceId = null
                selectedPoi = null
            }
        )
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(location)
                        .zoom(16f)
                        .build()
                ),
                durationMs = 900
            )
        }
    }

    // move camera to selected location
    LaunchedEffect(selectedPoi) {
        selectedPoi?.let { poi ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(poi.latLng)
                        .zoom(16f)
                        .build()
                ),
                durationMs = 1000
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
                selectedPoi = SelectedPoi(
                    placeId = poi.placeId,
                    name = poi.name,
                    latLng = poi.latLng
                )
            }
        ) {
            userLocation?.let { location ->
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
                    state = MarkerState(position = poi.latLng),
                    title = poi.name
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

        // --- Overlay: Footer (single nearby card with pager dots) ---
        viewModel?.let { vm ->
            val routes = vm.nearbyRoutes
            if (routes.isNotEmpty()) {
                if (currentNearbyIndex !in routes.indices) {
                    currentNearbyIndex = 0
                }
                NearbyAreasOverlay(
                    route = routes[currentNearbyIndex],
                    index = currentNearbyIndex,
                    total = routes.size,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = GoTouchGrassDimens.SpacingMd,
                            end = GoTouchGrassDimens.SpacingMd,
                            bottom = 72.dp
                        ),
                    onNext = {
                        currentNearbyIndex = (currentNearbyIndex + 1) % routes.size
                    }
                )
            }
        }

        selectedPoi?.let { poi ->
            val info = remember(poi.placeId, poi.name) { selectedPoiInfo(poi) }
            val resolvedCapturePlaceId = remember(poi.placeId) { resolveCapturePlaceIdForDemo(poi.placeId) }
            val distanceResult = FloatArray(1)
            val distanceMeters = userLocation?.let { location ->
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
                info = info,
                isNearby = isNearby,
                distanceMeters = distanceMeters,
                isCaptured = capturedPlaceIds.contains(resolvedCapturePlaceId),
                onCapture = {
                    selectedPoi = null
                    capturePlaceId = resolvedCapturePlaceId
                },
                onClose = { selectedPoi = null }
            )
        }

        if (!isLocationPermissionGranted) {
            LocationRequiredOverlay(
                message = "Location permission is required to use the map.",
                showPermissionAction = !isLocationPermissionGranted,
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun LocationRequiredOverlay(
    message: String,
    showPermissionAction: Boolean,
    onRequestPermission: () -> Unit
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
                    text = "Current location required",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
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
    modifier: Modifier = Modifier
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
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toString(),
                            fontSize = 10.sp,
                            color = onAccent
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
                        if (maxXp <= 0) 0f else (currentXp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = onSurface.copy(alpha = 0.08f),
                    drawStopIndicator = {}
                )

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
    route: com.example.gotouchgrass.domain.ExploreRouteItem,
    index: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onNext: () -> Unit
) {
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
                        text = "${route.theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} · nearby",
                        fontSize = 10.sp,
                        color = muted
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

                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Start",
                        fontSize = 11.sp,
                        color = onAccent
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
            ) {
                repeat(total) { i ->
                    val active = i == index
                    Box(
                        modifier = Modifier
                            .size(width = if (active) 18.dp else 6.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (active) accent else onSurface.copy(alpha = 0.15f))
                            .clickable { onNext() }
                    )
                }
            }
        }
    }
}


@Composable
private fun CapturePoiPopup(
    selectedPoi: SelectedPoi,
    info: SelectedPoiInfo,
    isNearby: Boolean,
    distanceMeters: Float?,
    isCaptured: Boolean,
    onCapture: () -> Unit,
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
                    text = selectedPoi.name,
                    style = MaterialTheme.typography.titleMedium
                )

                info.zoneName?.let { zoneName ->
                    Text(
                        text = "Zone: $zoneName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                info.categoryName?.let { categoryName ->
                    Text(
                        text = "Category: $categoryName",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                info.ownerDisplayName?.let { ownerDisplayName ->
                    Text(
                        text = "Owned by: $ownerDisplayName",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = when {
                        isCaptured -> "Already captured."
                        distanceMeters == null -> "Location unavailable."
                        isNearby -> "Nearby (${distanceMeters.toInt()}m)"
                        else -> "Move closer (${distanceMeters.toInt()}m)"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                ) {
                    Button(
                        onClick = onCapture,
                        enabled = isNearby && !isCaptured,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when {
                                isCaptured -> "Captured"
                                isNearby -> "Capture"
                                else -> "Not Nearby"
                            }
                        )
                    }

                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
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
        MapScreen(viewModel = vm)
    }
}
