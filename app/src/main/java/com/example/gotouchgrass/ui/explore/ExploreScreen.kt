package com.example.gotouchgrass.ui.explore

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.R
import com.example.gotouchgrass.ui.theme.ForestGreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.ui.theme.GoldenYellow
import com.example.gotouchgrass.ui.theme.GoldenYellowDark
import com.example.gotouchgrass.ui.theme.SandLight
import com.example.gotouchgrass.ui.theme.XpBarStart


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: ExploreViewModel) {

    val scrollState = rememberScrollState()
    var selectedRoute by remember { mutableStateOf<RouteCardData?>(null) }

    Column(
        modifier = Modifier
            .padding(horizontal = GoTouchGrassDimens.SpacingMd)
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingMd)
    ) {
        // Header
        Row(
            modifier = Modifier
                .padding(top = GoTouchGrassDimens.SpacingMd)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Explore", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

            Surface(
                shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
                color = SandLight
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = GoTouchGrassDimens.SpacingSm,
                        vertical = GoTouchGrassDimens.SpacingXs
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingXs)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star_24),
                        contentDescription = "XP",
                        tint = GoldenYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${viewModel.totalXP} XP",
                        style = MaterialTheme.typography.labelLarge,
                        color = ForestGreen
                    )
                }
            }
        }

        // Daily Challenges
        Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.target_24),
                    contentDescription = "Target",
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Daily Challenges",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            viewModel.dailyChallenges.forEach { cardData ->
                ChallengeCard(
                    card = cardData)
            }
        }

        // Weekly Challenges
        Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.star_24),
                    contentDescription = "Star",
                    tint = GoldenYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Weekly Challenges",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            viewModel.weeklyChallenges.forEach { cardData ->
                ChallengeCard(
                    card = cardData)
            }
        }

        // Curated Routes
        Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.explore_24),
                    contentDescription = "Explore",
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Curated Routes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            viewModel.curatedRoutes.forEach { cardData ->
                RouteCard(
                    card = cardData,
                    onClick = { selectedRoute = cardData }
                )
            }
        }

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))
    }

    selectedRoute?.let { route ->
        RouteInfoPopup(
            route = route,
            onClose = { selectedRoute = null }
        )
    }
}

@Composable
fun ChallengeCard(
    modifier: Modifier = Modifier,
    card: ChallengeCardData
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(GoTouchGrassDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = card.challengeType.iconRes()),
                contentDescription = "Challenge Type",
                tint = ForestGreen,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(GoTouchGrassDimens.SpacingMd))

            // Left side
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingXs)
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(card.progress, style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { card.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = XpBarStart,
                    trackColor = SandLight,
                    drawStopIndicator = { }
                )
            }

            Spacer(Modifier.width(GoTouchGrassDimens.SpacingMd))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "+${card.reward} XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldenYellowDark
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "${(card.progressFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RouteInfoPopup(
    route: RouteCardData,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            .padding(GoTouchGrassDimens.SpacingMd)
    ) {

        ElevatedCard(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GoTouchGrassDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                Text(
                    text = route.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text("Theme: ${route.theme.name}", style = MaterialTheme.typography.bodySmall)
                Text("Difficulty: ${route.difficulty.name}", style = MaterialTheme.typography.bodySmall)
                Text("Zones: ${route.zoneCount}", style = MaterialTheme.typography.bodySmall)
                Text("Estimated time: ${route.hours} hours", style = MaterialTheme.typography.bodySmall)

                if (route.routeStops.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))
                    Text(
                        text = "Route Stops",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    route.routeStops.forEach { stop ->
                        Text(
                            text = "$stop",
                            style = MaterialTheme.typography.bodySmall
                        )
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

@Composable
fun RouteCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    card: RouteCardData
) {
    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = card.theme.iconRes()),
                contentDescription = "Route Type",
                tint = ForestGreen,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(GoTouchGrassDimens.SpacingMd))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Show Zones
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.location_on_24),
                            contentDescription = "Location",
                            tint = ForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${card.zoneCount} zones",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Show time estimate
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.schedule_24),
                            contentDescription = "Clock",
                            tint = ForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "~${card.hours} hours",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Show difficulty
                    Surface(
                        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
                        color = SandLight
                    ) {
                        Text(
                            text = card.difficulty.name,
                            modifier = Modifier.padding(
                                horizontal = GoTouchGrassDimens.SpacingSm,
                                vertical = GoTouchGrassDimens.SpacingXs
                            ),
                            color = card.difficulty.color(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview() {
    GoTouchGrassTheme {
        ExploreScreen(viewModel = ExploreViewModel())
    }
}