package com.example.gotouchgrass.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens

private val GoldenYellow = Color(0xFFE8B931)
private val ErrorRed = Color(0xFFD32F2F)
private val ErrorRedLight = Color(0xFFFFFFFF)

@Composable
fun ActiveTripBar(
    elapsedSeconds: Long,
    distanceMeters: Float,
    xpEarned: Int,
    captureCount: Int,
    routeName: String?,
    onEndTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardSurface = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cardSurface,
        border = BorderStroke(0.5.dp, onSurface.copy(alpha = 0.12f)),
        tonalElevation = GoTouchGrassDimens.ElevationNone
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Route name or "Free Roam" label
            Text(
                text = if (routeName != null) "On route: $routeName" else "Free Roam",
                style = MaterialTheme.typography.labelSmall,
                color = muted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TripStat(label = "Time", value = formatElapsed(elapsedSeconds))
                    TripStat(
                        label = "Dist",
                        value = if (distanceMeters >= 1000f) "%.1f km".format(distanceMeters / 1000f)
                        else "${distanceMeters.toInt()} m"
                    )
                    TripStat(label = "XP", value = "+$xpEarned", valueColor = GoldenYellow)
                    TripStat(label = "Captures", value = captureCount.toString())
                }

                // End trip button
                Button(
                    onClick = onEndTrip,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = ErrorRedLight
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp, vertical = 4.dp
                    )
                ) {
                    Text(text = "End", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TripStat(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%d:%02d".format(m, s)
}
