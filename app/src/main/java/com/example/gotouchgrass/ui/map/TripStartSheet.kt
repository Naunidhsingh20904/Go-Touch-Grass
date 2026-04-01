package com.example.gotouchgrass.ui.map

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens

@Composable
fun TripStartSheet(
    availableRoutes: List<ExploreRouteItem>,
    onStartFreeRoam: () -> Unit,
    onStartRoute: (ExploreRouteItem) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}, // consume clicks so they don't bubble to dismiss
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = GoTouchGrassDimens.SpacingMd)
            ) {
                // Handle + header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GoTouchGrassDimens.SpacingMd, vertical = 12.dp)
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.TopCenter)
                    )
                    Text(
                        text = "Start a Trip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(GoTouchGrassDimens.SpacingMd))

                // Free Roam option
                Column(
                    modifier = Modifier.padding(horizontal = GoTouchGrassDimens.SpacingMd)
                ) {
                    Text(
                        text = "Explore freely",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = onStartFreeRoam,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusMedium)
                    ) {
                        Text("Free Roam", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (availableRoutes.isNotEmpty()) {
                    Spacer(Modifier.height(GoTouchGrassDimens.SpacingMd))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = GoTouchGrassDimens.SpacingMd),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(GoTouchGrassDimens.SpacingMd))

                    Text(
                        text = "Or follow a route",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = GoTouchGrassDimens.SpacingMd)
                    )
                    Spacer(Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((availableRoutes.size * 72).coerceAtMost(280).dp)
                            .padding(horizontal = GoTouchGrassDimens.SpacingMd),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableRoutes) { route ->
                            RouteOptionRow(route = route, onStart = { onStartRoute(route) })
                        }
                    }
                }

                Spacer(Modifier.height(GoTouchGrassDimens.SpacingMd))
            }
        }
    }
}

@Composable
private fun RouteOptionRow(
    route: ExploreRouteItem,
    onStart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Route,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = route.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "${route.zoneCount} stops · ~${
                    if (route.hours < 1) "${(route.hours * 60).toInt()}m"
                    else "%.1fh".format(route.hours)
                }",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onStart,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 12.dp, vertical = 4.dp
            )
        ) {
            Text("Start", fontSize = 12.sp)
        }
    }

    Spacer(Modifier.width(0.dp)) // spacer between items handled by LazyColumn
}
