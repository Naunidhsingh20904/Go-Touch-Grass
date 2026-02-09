package com.example.gotouchgrass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gotouchgrass.ui.theme.*

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GoTouchGrassDimens.SpacingMd)
    ) {
        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Top Header
        ProfileHeader(onSettingsClick = onSettingsClick)

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Profile Overview Card
        ProfileOverviewCard(
            username = "YourUsername",
            joinedText = "Joined 3 months ago",
            streakDays = 7,
            level = 8,
            currentXp = 450,
            maxXp = 1000
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Quick Stats Grid
        QuickStatsGrid()

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Badges Section
        BadgesSection()

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Recent Activity Section
        RecentActivitySection()

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Friends Section
        FriendsSection()

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))
    }
}

@Composable
private fun ProfileHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ProfileOverviewCard(
    username: String,
    joinedText: String,
    streakDays: Int,
    level: Int,
    currentXp: Int,
    maxXp: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with level badge
                Box {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(GoTouchGrassDimens.RadiusMedium))
                            .background(ForestGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = username.first().uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = WarmWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Level badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(GoldenYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingMd))

                // User info
                Column {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = joinedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\uD83D\uDD25",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingXs))
                        Text(
                            text = "$streakDays day streak",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

            // Level Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$currentXp / $maxXp XP",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

                LinearProgressIndicator(
                    progress = { currentXp.toFloat() / maxXp.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(GoTouchGrassDimens.RadiusFull)),
                    color = ForestGreen,
                    trackColor = SandLight
                )

                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))

                Text(
                    text = "${maxXp - currentXp} XP to Level ${level + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickStatsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
        ) {
            StatCard(
                icon = Icons.Default.LocationOn,
                value = "24",
                label = "Zones Visited",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Star,
                value = "8",
                label = "Zones Owned",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
        ) {
            StatCard(
                icon = Icons.Default.DateRange,
                value = "47h",
                label = "Time Explored",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Check,
                value = "12",
                label = "Challenges Done",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BadgesSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldenYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingSm))
                    Text(
                        text = "Badges",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = { }) {
                    Text(
                        text = "View All >",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

            // Badge grid 3x2
            val activeBadges = listOf(
                BadgeInfo(Icons.Default.ThumbUp, "First Steps", true),
                BadgeInfo(Icons.Default.Place, "Explorer", true),
                BadgeInfo(Icons.Default.Star, "Night Owl", true)
            )
            val lockedBadges = listOf(
                BadgeInfo(Icons.Default.Place, "Globe Trotter", false),
                BadgeInfo(Icons.Default.Favorite, "Champion", false),
                BadgeInfo(Icons.Default.Build, "Legendary", false)
            )

            Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    activeBadges.forEach { badge ->
                        BadgeItem(badge = badge)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    lockedBadges.forEach { badge ->
                        BadgeItem(badge = badge)
                    }
                }
            }
        }
    }
}

private data class BadgeInfo(
    val icon: ImageVector,
    val name: String,
    val isUnlocked: Boolean
)

@Composable
private fun BadgeItem(badge: BadgeInfo) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (badge.isUnlocked) ForestGreen else SandMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = badge.name,
                tint = if (badge.isUnlocked) WarmWhite else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))

        Text(
            text = badge.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (badge.isUnlocked)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentActivitySection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingSm))
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

            val activities = listOf(
                ActivityItem("MC Building", "2h ago", "+120 XP"),
                ActivityItem("SLC", "5h ago", "+85 XP"),
                ActivityItem("Lazeez", "1d ago", "+250 XP")
            )

            Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                activities.forEach { activity ->
                    ActivityRow(activity = activity)
                }
            }
        }
    }
}

private data class ActivityItem(
    val name: String,
    val time: String,
    val xp: String
)

@Composable
private fun ActivityRow(activity: ActivityItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusMedium),
        color = SandLight
    ) {
        Row(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingSm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activity.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = activity.xp,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = ForestGreen
            )
        }
    }
}

@Composable
private fun FriendsSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GoTouchGrassDimens.RadiusLarge),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingSm))
                    Text(
                        text = "Friends",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = { }) {
                    Text(
                        text = "Find Friends >",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
            ) {
                val friends = listOf("A", "B", "C", "D", "E")
                friends.forEach { initial ->
                    FriendAvatar(initial = initial)
                }

                // +12 more
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SandMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+12",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendAvatar(initial: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(ForestGreen),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = WarmWhite
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    GoTouchGrassTheme {
        ProfileScreen()
    }
}
