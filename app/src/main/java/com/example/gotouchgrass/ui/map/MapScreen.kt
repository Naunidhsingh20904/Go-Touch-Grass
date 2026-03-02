package com.example.gotouchgrass.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gotouchgrass.ui.theme.ForestGreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.ui.theme.SandLight
import com.example.gotouchgrass.ui.theme.WarmWhite
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// Zone class to ease UI

data class Zone(
    val id: String,
    val title: String,
    val owner: String,
    val since: String,
    val activeMembers: List<Int>, // drawable IDs
    val currentScore: Int,
    val maxScore: Int,
    val position: LatLng,
    val zoneName: String? = null
)

private data class ZoneBounds(
    val name: String,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)

private val UW_ZONES = listOf(
    ZoneBounds(
        name = "Engineering Zone",
        minLat = 43.4708, maxLat = 43.4732,
        minLng = -80.5462, maxLng = -80.5418
    ),
    ZoneBounds(
        name = "Math Zone",
        minLat = 43.4718, maxLat = 43.4742,
        minLng = -80.5474, maxLng = -80.5438
    ),
    ZoneBounds(
        name = "Arts Zone",
        minLat = 43.4688, maxLat = 43.4714,
        minLng = -80.5460, maxLng = -80.5420
    )
)

data class NearbyPerson(
    val name: String,
    val position: LatLng
)


private fun zoneForLatLng(p: LatLng): String? {
    return UW_ZONES.firstOrNull { z ->
        p.latitude in z.minLat..z.maxLat && p.longitude in z.minLng..z.maxLng
    }?.name
}


@Composable
fun MapScreen() {
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    val context = LocalContext.current
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLatLng by remember { mutableStateOf<LatLng?>(null) }

    var capturedIds by remember { mutableStateOf(setOf<String>()) }

    fun fetchLastLocationIfPermitted() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) return

        try {
            fused.lastLocation.addOnSuccessListener { loc ->
                loc?.let { userLatLng = LatLng(it.latitude, it.longitude) }
            }
        } catch (_: SecurityException) {
            // ignore
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLastLocationIfPermitted()
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            fetchLastLocationIfPermitted()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val uw = LatLng(43.4723, -80.5449)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uw, 15f)
    }

    LaunchedEffect(userLatLng) {
        userLatLng?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
        }
    }

    val base = userLatLng ?: LatLng(43.4723, -80.5449)
    val nearbyPeople = remember(base) {
        listOf(
            NearbyPerson("Kenny", LatLng(base.latitude + 0.0006, base.longitude + 0.0004)),
            NearbyPerson("Stan", LatLng(base.latitude - 0.0004, base.longitude + 0.0007)),
            NearbyPerson("Kyle", LatLng(base.latitude + 0.0002, base.longitude - 0.0008)),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = userLatLng != null),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
            ),
            onPOIClick = { poi ->
                val id = poi.placeId
                val captured = capturedIds.contains(id)

                selectedZone = Zone(
                    id = id,
                    title = poi.name,
                    owner = if (captured) "You" else "Unclaimed",
                    since = if (captured) "Just now" else "N/A",
                    activeMembers = emptyList(),
                    currentScore = if (captured) 100 else 0,
                    maxScore = 100,
                    position = poi.latLng,
                    zoneName = zoneForLatLng(poi.latLng)
                )
            }
        ) {
            nearbyPeople.forEach { person ->
                Marker(
                    state = MarkerState(person.position),
                    title = person.name,
                    snippet = "Nearby"
                )
            }
        }

        TopHud(
            xpProgress = 0.72f,
        )


        selectedZone?.let { zone ->
            ZonePopup(
                zone = zone,
                isCaptured = capturedIds.contains(zone.id),
                onCapture = {
                    capturedIds = capturedIds + zone.id
                    selectedZone = zone.copy(
                        owner = "You",
                        since = "Just now",
                        currentScore = 100
                    )
                },
                onClose = { selectedZone = null }
            )
        }

    }
}

////////////////////////////////////////////////////////////
// TOP HUD
////////////////////////////////////////////////////////////

@Composable
private fun TopHud(
    xpProgress: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        // Black background container
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // XP Bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray)
            ) {
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = ForestGreen,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}


@Composable
private fun ZonePopup(
    zone: Zone,
    isCaptured: Boolean,
    onCapture: () -> Unit,
    onClose: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() }
    ) {

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .width(330.dp)
                    .padding(16.dp)
            ) {

                // Title Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ForestGreen)
                        .padding(8.dp)
                ) {
                    Text(
                        text = zone.title,
                        color = WarmWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                zone.zoneName?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Zone: $it", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Currently Owned By",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = zone.owner,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Since ${zone.since}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = zone.currentScore.toFloat() / zone.maxScore.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = ForestGreen,
                    trackColor = SandLight
                )

                Text(
                    text = "${zone.currentScore}/${zone.maxScore}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Active Members:")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    zone.activeMembers.forEach { drawable ->
                        Image(
                            painter = painterResource(id = drawable),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = if (isCaptured) onClose else onCapture,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isCaptured) "Captured ✓" else "Capture")
                }

                Spacer(modifier = Modifier.height(8.dp))

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
        MapScreen()
    }
}
