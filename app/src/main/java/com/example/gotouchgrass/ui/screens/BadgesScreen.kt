package com.example.gotouchgrass.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.ui.theme.ForestGreen
import com.example.gotouchgrass.ui.theme.ForestGreenDark
import com.example.gotouchgrass.ui.theme.GoldenYellow
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.WarmWhite

private fun iconForKey(key: String): ImageVector = when (key) {
    "location_on"  -> Icons.Default.LocationOn
    "star"         -> Icons.Default.Star
    "explore"      -> Icons.Default.Place
    "thumb_up"     -> Icons.Default.ThumbUp
    "favorite"     -> Icons.Default.Favorite
    "trending_up"  -> Icons.Default.TrendingUp
    "date_range"   -> Icons.Default.DateRange
    "place"        -> Icons.Default.Place
    else           -> Icons.Default.Star
}

@Composable
fun BadgesScreen(
    badges: List<ProfileViewModel.ProfileBadgeDisplay>,
    onBackClick: () -> Unit
) {
    val unlockedCount = badges.count { it.isUnlocked }
    val totalCount = badges.size
    val progressFraction = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "badgeProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ForestGreenDark, ForestGreen)
                    )
                )
                .padding(top = GoTouchGrassDimens.SpacingMd, bottom = GoTouchGrassDimens.SpacingLg)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = GoTouchGrassDimens.SpacingSm)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmWhite
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldenYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = WarmWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$unlockedCount of $totalCount unlocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmWhite.copy(alpha = 0.8f)
                )
            }
        }

        // Progress bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ForestGreen
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GoldenYellow,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

        // Badge grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(GoTouchGrassDimens.SpacingMd),
            horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingMd),
            modifier = Modifier.fillMaxSize()
        ) {
            items(badges) { badge ->
                BadgeCard(badge = badge)
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: ProfileViewModel.ProfileBadgeDisplay) {
    val icon = iconForKey(badge.iconKey)

    Surface(
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (badge.isUnlocked) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (badge.isUnlocked) 1f else 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked)
                            Brush.radialGradient(listOf(GoldenYellow, ForestGreen))
                        else
                            Brush.radialGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = badge.name,
                    tint = if (badge.isUnlocked) WarmWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

            Text(
                text = badge.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (badge.isUnlocked) ForestGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant
            ) {
                Text(
                    text = if (badge.isUnlocked) "Unlocked" else "Locked",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (badge.isUnlocked) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
