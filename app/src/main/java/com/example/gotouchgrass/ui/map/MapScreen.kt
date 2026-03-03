package com.example.gotouchgrass.ui.map

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.ui.map.capture.CaptureScreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private const val CAPTURE_RADIUS_METERS = 100f
private const val DEMO_EGG_FOUNTAIN_ID = "lm_uw_egg_fountain"
private val UW_DEMO_LOCATION = LatLng(43.4723, -80.5449)

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
fun MapScreen() {
    var capturePlaceId by remember { mutableStateOf<String?>(null) }
    var selectedPoi by remember { mutableStateOf<SelectedPoi?>(null) }
    var capturedPlaceIds by remember { mutableStateOf(setOf<String>()) }

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

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = false,
            ),
            onPOIClick = { poi ->
                selectedPoi = SelectedPoi(
                    placeId = poi.placeId,
                    name = poi.name,
                    latLng = poi.latLng
                )
            }
        )

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
        MapScreen()
    }
}
