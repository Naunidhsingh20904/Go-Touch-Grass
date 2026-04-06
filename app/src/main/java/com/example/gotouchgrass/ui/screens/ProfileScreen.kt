package com.example.gotouchgrass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gotouchgrass.domain.avatarDrawableResForKey
import com.example.gotouchgrass.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSettingsClick: () -> Unit = {},
    onFindFriendsClick: () -> Unit = {}
) {
    var showAllBadgesDialog by remember { mutableStateOf(false) }

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
            displayName = viewModel.displayName,
            username = viewModel.username,
            avatarKey = viewModel.avatarKey,
            joinedText = viewModel.joinedText,
            streakDays = viewModel.streakDays,
            level = viewModel.level,
            currentXp = viewModel.currentXp,
            maxXp = viewModel.maxXp
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Quick Stats Grid
        QuickStatsGrid(
            zonesVisited = viewModel.zonesVisited,
            zonesOwned = viewModel.zonesOwned,
            timeExplored = viewModel.timeExploredHours,
            challengesDone = viewModel.challengesDone
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Badges Section
        BadgesSection(
            badges = viewModel.badges,
            onViewAll = { showAllBadgesDialog = true }
        )

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Recent Activity Section
        RecentActivitySection(activities = viewModel.recentActivity)

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingMd))

        // Friends Section
        FriendsSection(friendInitials = viewModel.friendInitials, onFindFriendsClick = onFindFriendsClick)

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingLg))
    }

    if (showAllBadgesDialog) {
        AllBadgesDialog(
            badges = viewModel.badges,
            onDismiss = { showAllBadgesDialog = false }
        )
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
    displayName: String,
    username: String,
    avatarKey: String?,
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
                    val avatarRes = avatarDrawableResForKey(avatarKey)
                    if (avatarRes != null) {
                        Image(
                            painter = painterResource(id = avatarRes),
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(GoTouchGrassDimens.RadiusMedium))
                                .background(ForestGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = username.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineLarge,
                                color = WarmWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        text = if (displayName.isBlank()) username else displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    trackColor = SandLight,
                    drawStopIndicator = {}
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
private fun QuickStatsGrid(
    zonesVisited: String,
    zonesOwned: String,
    timeExplored: String,
    challengesDone: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
        ) {
            StatCard(
                icon = Icons.Default.LocationOn,
                value = zonesVisited,
                label = "Zones Visited",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Star,
                value = zonesOwned,
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
                value = timeExplored,
                label = "Time Explored",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Check,
                value = challengesDone,
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

private val badgeIcons = listOf(
    Icons.Default.ThumbUp,
    Icons.Default.Place,
    Icons.Default.Star,
    Icons.Default.Place,
    Icons.Default.Favorite,
    Icons.Default.Star
)

@Composable
private fun BadgesSection(
    badges: List<ProfileViewModel.ProfileBadgeDisplay>,
    onViewAll: () -> Unit = {}
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

                if (badges.size > 6) {
                    TextButton(onClick = onViewAll) {
                        Text(
                            text = "View All >",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

            val displayBadges = badges.take(6)
            Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    displayBadges.take(3).forEachIndexed { index, badge ->
                        BadgeItem(
                            icon = badgeIcons.getOrElse(index) { Icons.Default.Star },
                            name = badge.name,
                            isUnlocked = badge.isUnlocked
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    displayBadges.drop(3).forEachIndexed { index, badge ->
                        BadgeItem(
                            icon = badgeIcons.getOrElse(3 + index) { Icons.Default.Star },
                            name = badge.name,
                            isUnlocked = badge.isUnlocked
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    icon: ImageVector,
    name: String,
    isUnlocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) ForestGreen else SandMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isUnlocked) WarmWhite else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingXs))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AllBadgesDialog(
    badges: List<ProfileViewModel.ProfileBadgeDisplay>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldenYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(GoTouchGrassDimens.SpacingSm))
                Text(
                    text = "All Badges",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            if (badges.isEmpty()) {
                Text(
                    text = "No badges earned yet. Keep exploring to unlock achievements!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingMd)
                ) {
                    val rows = badges.chunked(3)
                    rows.forEach { rowBadges ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowBadges.forEachIndexed { index, badge ->
                                BadgeItem(
                                    icon = badgeIcons.getOrElse(index) { Icons.Default.Star },
                                    name = badge.name,
                                    isUnlocked = badge.isUnlocked
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ForestGreen)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun RecentActivitySection(
    activities: List<ProfileViewModel.ActivityItemDisplay>
) {
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

            if (activities.isEmpty()) {
                Text(
                    text = "No recent activity yet. Start exploring to earn XP!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = GoTouchGrassDimens.SpacingSm)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                    activities.forEach { activity ->
                        ActivityRow(
                            name = activity.name,
                            timeAgo = activity.timeAgo,
                            xpText = activity.xpText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    name: String,
    timeAgo: String,
    xpText: String
) {
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
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = xpText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = ForestGreen
            )
        }
    }
}

@Composable
private fun FriendsSection(friendInitials: List<String>, onFindFriendsClick: () -> Unit = {}) {
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

                TextButton(onClick = onFindFriendsClick) {
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
                friendInitials.forEach { initial ->
                    FriendAvatar(initial = initial)
                }

                if (friendInitials.isEmpty()) {
                    Text(
                        text = "No friends yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (friendInitials.size > 6) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SandMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${friendInitials.size - 6}",
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

// Note: The preview keeps using a no‑args stub ViewModel from FakeData
// to avoid requiring a running database connection in design tools.
