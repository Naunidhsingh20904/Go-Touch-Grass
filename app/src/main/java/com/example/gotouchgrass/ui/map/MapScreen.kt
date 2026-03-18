package com.example.gotouchgrass.ui.map

import android.Manifest
import android.location.Location
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.data.FakeMapRepository
import com.example.gotouchgrass.data.FakeProfileRepository
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.MapModel
import com.example.gotouchgrass.ui.map.capture.CaptureScreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.ForestGreen
import com.example.gotouchgrass.ui.theme.GoldenYellow
import com.example.gotouchgrass.ui.theme.SandLight
import com.example.gotouchgrass.ui.theme.XpBarStart
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.FetchPlaceRequest

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
    onPlaceLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLocationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isLocationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            hasLocationPermission(context)
    }

    LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var capturePlaceId by remember { mutableStateOf<String?>(null) }
    var selectedPoi by remember { mutableStateOf<SelectedPoi?>(null) }
    var capturedPlaceIds by remember { mutableStateOf(setOf<String>()) }

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
        position = CameraPosition.fromLatLngZoom(UW_DEMO_LOCATION, 16f)
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
            properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = isLocationPermissionGranted,
            ),
            onPOIClick = { poi ->
                selectedPoi = SelectedPoi(
                    placeId = poi.placeId,
                    name = poi.name,
                    latLng = poi.latLng
                )
            }
        ) {
            // add marker at selected location
            selectedPoi?.let { poi ->
                Marker(
                    state = MarkerState(position = poi.latLng),
                    title = poi.name
                )
            }
        }

        // --- Overlay: Header (progress + motivating stats) ---
        viewModel?.let { vm ->
            val header = vm.headerStats
            MapHeaderOverlay(
                level = header.level,
                currentXp = header.currentXp,
                maxXp = header.maxXp,
                xpToNext = header.xpToNextLevel,
                totalXp = header.totalXp,
                streakDays = header.streakDays,
                zonesVisited = header.zonesVisited,
                timeOutside = header.timeOutsideLabel,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = GoTouchGrassDimens.SpacingMd,
                        start = GoTouchGrassDimens.SpacingMd,
                        end = GoTouchGrassDimens.SpacingMd
                    )
            )
        }

        // --- Overlay: Footer (nearby areas) ---
        viewModel?.let { vm ->
            if (vm.nearbyRoutes.isNotEmpty()) {
                NearbyAreasOverlay(
                    title = "Nearby",
                    routes = vm.nearbyRoutes.take(6),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = GoTouchGrassDimens.SpacingMd,
                            end = GoTouchGrassDimens.SpacingMd,
                            bottom = GoTouchGrassDimens.SpacingMd
                        )
                )
            }
        }

        selectedPoi?.let { poi ->
            val info = remember(poi.placeId, poi.name) { selectedPoiInfo(poi) }
            val resolvedCapturePlaceId = remember(poi.placeId) { resolveCapturePlaceIdForDemo(poi.placeId) }
            val distanceResult = FloatArray(1)
            Location.distanceBetween(
                UW_DEMO_LOCATION.latitude,
                UW_DEMO_LOCATION.longitude,
                poi.latLng.latitude,
                poi.latLng.longitude,
                distanceResult
            )
            val distanceMeters = distanceResult[0]
            val isNearby = distanceMeters <= CAPTURE_RADIUS_METERS
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
    }
}

@Composable
private fun MapHeaderOverlay(
    level: Int,
    currentXp: Int,
    maxXp: Int,
    xpToNext: Int,
    totalXp: Int,
    streakDays: Int,
    zonesVisited: Int,
    timeOutside: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
                        color = SandLight
                    ) {
                        Text(
                            text = level.toString(),
                            modifier = Modifier.padding(
                                horizontal = GoTouchGrassDimens.SpacingSm,
                                vertical = GoTouchGrassDimens.SpacingXs
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreen
                        )
                    }

                    Spacer(Modifier.size(GoTouchGrassDimens.SpacingSm))

                    Text(
                        text = "Level $level Explorer",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$xpToNext to next",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreen
                )
            }

            LinearProgressIndicator(
                progress = {
                    if (maxXp <= 0) 0f else (currentXp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(GoTouchGrassDimens.RadiusFull)),
                color = XpBarStart,
                trackColor = SandLight,
                drawStopIndicator = {}
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                StatPill(label = "XP", value = "%,d".format(totalXp), accent = GoldenYellow, modifier = Modifier.weight(1f))
                StatPill(label = "Streak", value = streakDays.toString(), accent = ForestGreen, modifier = Modifier.weight(1f))
                StatPill(label = "Zones", value = zonesVisited.toString(), accent = ForestGreen, modifier = Modifier.weight(1f))
                StatPill(label = "Time", value = timeOutside, accent = ForestGreen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
        color = SandLight
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GoTouchGrassDimens.SpacingSm,
                vertical = GoTouchGrassDimens.SpacingXs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
            Spacer(Modifier.size(GoTouchGrassDimens.SpacingXs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NearbyAreasOverlay(
    title: String,
    routes: List<com.example.gotouchgrass.domain.ExploreRouteItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "tap a spot",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreen
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                routes.forEach { route ->
                    NearbyRouteCard(route = route)
                }
            }
        }
    }
}

@Composable
private fun NearbyRouteCard(route: com.example.gotouchgrass.domain.ExploreRouteItem) {
    Card(
        modifier = Modifier
            .size(width = 190.dp, height = 92.dp),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        colors = CardDefaults.cardColors(containerColor = SandLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = route.theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = ForestGreen
            )
            Text(
                text = route.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${route.zoneCount} stops • ~${route.hours}h",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
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
