package com.example.gotouchgrass.ui.screens

import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.domain.FakeData
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val CURRENT_USER_ID = "user_you"
private const val XP_PER_LEVEL = 1000

/**
 * ViewModel for the Profile screen. Loads profile, milestones, badges, and stats
 * from [FakeData] for the current user.
 */
class ProfileViewModel : ViewModel() {

    private val user = FakeData.users.find { it.id == CURRENT_USER_ID }
    private val userStreaks = FakeData.streaks.filter { it.userId == CURRENT_USER_ID }
    private val userBadgeIds = FakeData.userBadges
        .filter { it.userId == CURRENT_USER_ID }
        .map { it.badgeId }
        .toSet()
    private val userMilestoneProgress = FakeData.milestoneProgress.filter { it.userId == CURRENT_USER_ID }
    private val userVisitSessions = FakeData.visitSessions.filter { it.userId == CURRENT_USER_ID }
    private val userXpEvents = FakeData.xpEvents.filter { it.userId == CURRENT_USER_ID }
    private val userZoneOwnership = FakeData.zoneOwnership.filter { it.ownerUserId == CURRENT_USER_ID }
    private val userCityCompletion = FakeData.cityCompletion.find { it.userId == CURRENT_USER_ID }
    private val userChallengeProgress = FakeData.challengeProgress.filter { it.userId == CURRENT_USER_ID }
    private val acceptedFriendIds = FakeData.friendships
        .filter { it.status == com.example.gotouchgrass.domain.FriendshipStatus.ACCEPTED }
        .filter { it.requesterId == CURRENT_USER_ID || it.addresseeId == CURRENT_USER_ID }
        .map { if (it.requesterId == CURRENT_USER_ID) it.addresseeId else it.requesterId }

    val username: String
        get() = user?.username ?: ""

    val joinedText: String
        get() = user?.createdAtIso?.let { formatJoined(it) } ?: ""

    val streakDays: Int
        get() = userStreaks
            .find { it.type == com.example.gotouchgrass.domain.StreakType.DAILY_EXPLORE }
            ?.currentCount ?: 0

    val level: Int
        get() = user?.level ?: 1

    val currentXp: Int
        get() {
            val total = user?.xpTotal ?: 0
            val previousLevelsXp = (level - 1) * XP_PER_LEVEL
            return (total - previousLevelsXp).coerceIn(0, XP_PER_LEVEL)
        }

    val maxXp: Int
        get() = XP_PER_LEVEL

    val zonesVisited: String
        get() = (userCityCompletion?.zonesVisitedCount ?: userVisitSessions.map { it.zoneId }.toSet().size).toString()

    val zonesOwned: String
        get() = userZoneOwnership.size.toString()

    val timeExploredHours: String
        get() {
            val totalSec = userVisitSessions.sumOf { it.durationSec }
            val hours = totalSec / 3600.0
            return if (hours >= 1) "${hours.toInt()}h" else "${(totalSec / 60).toInt()}m"
        }

    val challengesDone: String
        get() = userChallengeProgress.count { it.completedAtIso != null }.toString()

    data class ProfileBadgeDisplay(val name: String, val isUnlocked: Boolean)

    val badges: List<ProfileBadgeDisplay>
        get() {
            val earned = FakeData.badges.filter { it.id in userBadgeIds }.map { ProfileBadgeDisplay(it.name, true) }
            val locked = FakeData.badges.filter { it.id !in userBadgeIds }.map { ProfileBadgeDisplay(it.name, false) }
            return earned + locked
        }

    data class ActivityItemDisplay(val name: String, val timeAgo: String, val xpText: String)

    val recentActivity: List<ActivityItemDisplay>
        get() = userXpEvents
            .sortedByDescending { it.createdAtIso }
            .take(5)
            .map { event ->
                ActivityItemDisplay(
                    name = resolveZoneName(event.refId),
                    timeAgo = formatTimeAgo(event.createdAtIso),
                    xpText = "+${event.amount} XP"
                )
            }

    val friendInitials: List<String>
        get() = acceptedFriendIds
            .mapNotNull { id -> FakeData.users.find { it.id == id }?.displayName }
            .map { name -> name.take(1).uppercase() }

    val milestoneProgressValue: Double?
        get() = userMilestoneProgress.firstOrNull()?.progressValue

    val milestoneProgressList: List<Pair<String, Double>>
        get() = userMilestoneProgress.map { it.milestoneId to it.progressValue }

    private fun resolveZoneName(refId: String?): String {
        if (refId == null) return "Activity"
        val session = FakeData.visitSessions.find { it.id == refId }
        val zoneId = session?.zoneId ?: return "Activity"
        return FakeData.zones.find { it.id == zoneId }?.name ?: "Activity"
    }

    private fun formatTimeAgo(iso: String): String {
        return try {
            val then = Instant.parse(iso)
            val now = Instant.now()
            val minutes = ChronoUnit.MINUTES.between(then, now)
            when {
                minutes < 60 -> "${minutes}m ago"
                minutes < 24 * 60 -> "${minutes / 60}h ago"
                else -> "${minutes / (24 * 60)}d ago"
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun formatJoined(createdAtIso: String): String {
        return try {
            val then = Instant.parse(createdAtIso)
            val now = Instant.now()
            val days = ChronoUnit.DAYS.between(then, now)
            when {
                days < 30 -> "Joined ${days} days ago"
                days < 365 -> "Joined ${days / 30} months ago"
                else -> "Joined ${days / 365} years ago"
            }
        } catch (_: Exception) {
            "Joined recently"
        }
    }
}
