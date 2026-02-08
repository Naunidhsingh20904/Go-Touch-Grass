package com.example.gotouchgrass.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.R
import com.example.gotouchgrass.ui.explore.ExploreViewModel
import com.example.gotouchgrass.ui.search.LocationCardData
import com.example.gotouchgrass.ui.search.color
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: ExploreViewModel) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Daily Challenges
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.target_24),
                    contentDescription = "Target",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Daily Challenges",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            viewModel.dailyChallenges.forEach { cardData ->
                ChallengeCard(
                    card = cardData,
                    onClick = {})
            }
        }

        // Weekly Challenges
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.star_24),
                    contentDescription = "Star",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = "Weekly Challenges",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            viewModel.weeklyChallenges.forEach { cardData ->
                ChallengeCard(
                    card = cardData,
                    onClick = {})
            }
        }

        // Curated Routes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.explore_24),
                    contentDescription = "Explore",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Curated Routes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            viewModel.curatedRoutes.forEach { cardData -> RouteCard(card = cardData, onClick = {}) }
        }
    }
}

@Composable
fun ChallengeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    card: ChallengeCardData
) {
    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = card.challengeType.iconRes()),
                contentDescription = "Challenge Type",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Left side
            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    modifier = Modifier.fillMaxWidth(),
                    drawStopIndicator = { }
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "+${card.reward} XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "${card.progressFraction * 100}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = card.routeType.iconRes()),
                contentDescription = "Route Type",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(12.dp))

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
                            tint = MaterialTheme.colorScheme.primary,
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
                            contentDescription = "Star",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "~${card.hours} hours",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Show difficulty
                    Card() {
                        Text(
                            card.difficulty.name,
                            color = card.difficulty.color(),
                            style = MaterialTheme.typography.bodySmall
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