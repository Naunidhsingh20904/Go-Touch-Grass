package com.example.gotouchgrass.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gotouchgrass.ui.theme.*
import com.example.gotouchgrass.R



// Zone class to ease UI

data class Zone(
    val title: String,
    val owner: String,
    val since: String,
    val activeMembers: List<Int>, // drawable IDs
    val currentScore: Int,
    val maxScore: Int
)



@Composable
fun MapScreen() {
    var selectedZone by remember { mutableStateOf<Zone?>(null) }

    val engineeringZone = Zone(
        title = "Faculty of Engineering @ UW",
        owner = "Kenny McCormick",
        since = "18/01/2026",
        activeMembers = listOf(
            R.drawable.active_user_1,
            R.drawable.active_user_1,
            R.drawable.active_user_1
        ),
        currentScore = 80,
        maxScore = 100
    )

    val artsZone = Zone(
        title = "Faculty of Arts @ UW",
        owner = "Stan Marsh",
        since = "03/02/2026",
        activeMembers = listOf(
            R.drawable.active_user_1,
            R.drawable.active_user_1
        ),
        currentScore = 40,
        maxScore = 100
    )

    val mathZone = Zone(
        title = "Faculty of Math @ UW",
        owner = "Kyle Broflovski",
        since = "11/03/2025",
        activeMembers = listOf(
            R.drawable.active_user_1,
            R.drawable.active_user_1,
            R.drawable.active_user_1,
            R.drawable.active_user_1
        ),
        currentScore = 95,
        maxScore = 100
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Map Background
        Image(
            painter = painterResource(id = R.drawable.map_placeholder),
            contentDescription = "Map",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        TopHud(
            xpProgress = 0.72f,
            coins = 346_769
        )

        MapPinsLayer(
            onZoneClick = { zone ->
                selectedZone = zone
            },
            engineeringZone = engineeringZone,
            artsZone = artsZone,
            mathZone = mathZone
        )


        BottomHub()

        MapUtilityButtons()

        selectedZone?.let { zone ->
            ZonePopup(
                zone = zone,
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
    coins: Int
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

            Spacer(modifier = Modifier.width(16.dp))

            // Coin counter
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ $coins",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldenYellow
                )
            }
        }
    }
}


////////////////////////////////////////////////////////////
// MAP PINS + USER
////////////////////////////////////////////////////////////

@Composable
private fun MapPinsLayer(
    onZoneClick: (Zone) -> Unit,
    engineeringZone: Zone,
    artsZone: Zone,
    mathZone: Zone
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Engineering (center)
        Image(
            painter = painterResource(id = R.drawable.user_avatar),
            contentDescription = "Engineering",
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .clickable { onZoneClick(engineeringZone) }
        )

        // Arts (top right)
        MapAvatar(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 60.dp, top = 180.dp)
                .clickable { onZoneClick(artsZone) },
            color = Color(0xFFFF6F61),
            icon = Icons.Default.Person
        )

        // Math (bottom left)
        MapAvatar(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 30.dp, bottom = 200.dp)
                .clickable { onZoneClick(mathZone) },
            color = Color(0xFF4DA6FF),
            icon = Icons.Default.Person
        )
    }
}




@Composable
private fun MapAvatar(
    modifier: Modifier,
    color: Color,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WarmWhite,
            modifier = Modifier.size(32.dp)
        )
    }
}

////////////////////////////////////////////////////////////
// BOTTOM HUB (Steps, +, ≡)
////////////////////////////////////////////////////////////

@Composable
private fun BottomHub() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        Surface(
            shape = RoundedCornerShape(40.dp),
            color = ForestGreen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(Icons.Default.Add, null, tint = WarmWhite)

                // Steps (center)
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = WarmWhite,
                    modifier = Modifier.size(32.dp)
                )

                Icon(Icons.Default.Menu, null, tint = WarmWhite)
            }
        }
    }
}

////////////////////////////////////////////////////////////
// MAP UTILITY BUTTONS (Compass + Locate Me)
////////////////////////////////////////////////////////////

@Composable
private fun MapUtilityButtons() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 20.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {

        Image(
            painter = painterResource(id = R.drawable.compass_icon),
            contentDescription = "Compass",
            modifier = Modifier
                .size(48.dp)
                .rotate(300f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FloatingActionButton(
            onClick = {},
            containerColor = Color.White,
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Locate Me",
                tint = ForestGreen
            )
        }
    }
}


@Composable
private fun ZonePopup(
    zone: Zone,
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
