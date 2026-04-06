package com.example.gotouchgrass.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.domain.ProfileModel
import com.example.gotouchgrass.domain.User
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_DAY = 86_400_000L
private const val MILLIS_PER_WEEK = 604_800_000L

private const val XP_PER_LEVEL = 1000

/**
 * Profile ViewModel backed by the domain‑level [ProfileModel], which in turn
 * talks to a [com.example.gotouchgrass.data.ProfileRepository] abstraction.
 *
 * This keeps the ViewModel free from direct DB details and allows the model
 * to be unit‑tested with a mock repository.
 */
class ProfileViewModel(
    private val model: ProfileModel
) : ViewModel() {

    var displayName by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var avatarKey by mutableStateOf<String?>(null)
        private set

    var joinedText by mutableStateOf("")
        private set

    var streakDays by mutableIntStateOf(0)
        private set

    var level by mutableStateOf(1)
        private set

    var currentXp by mutableIntStateOf(0)
        private set

    var maxXp by mutableIntStateOf(XP_PER_LEVEL)
        private set

    var zonesVisited by mutableStateOf("0")
        private set

    var zonesOwned by mutableStateOf("0")
        private set

    var timeExploredHours by mutableStateOf("0h")
        private set

    var challengesDone by mutableStateOf("0")
        private set

    data class ProfileBadgeDisplay(val name: String, val isUnlocked: Boolean)

    var badges by mutableStateOf<List<ProfileBadgeDisplay>>(emptyList())
        private set

    data class ActivityItemDisplay(val name: String, val timeAgo: String, val xpText: String)

    var recentActivity by mutableStateOf<List<ActivityItemDisplay>>(emptyList())
        private set

    var friendInitials by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                val user = model.getUser()
                val lifetimeStats = model.getLifetimeStats()
                val streakData = model.getStreakData()
                val weeklySummary = model.getWeeklySummary()
                val friends = model.getFriends()
                val totalCaptured = model.getTotalCapturedLandmarks()
                val recentActivityData = model.getRecentActivity()

                applyUser(user)
                applyStreak(streakData)
                applyLifetimeStats(lifetimeStats)
                applyWeeklySummary(weeklySummary)
                applyFriends(friends)
                zonesOwned = totalCaptured.toString()
                recentActivity = recentActivityData.map { activity ->
                    ActivityItemDisplay(
                        name = activity.displayName,
                        timeAgo = timeAgoFrom(activity.capturedAtIso),
                        xpText = "+${activity.xpAwarded} XP"
                    )
                }
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to load profile data"
            }

            isLoading = false
        }
    }

    suspend fun updateProfile(
        username: String,
        displayName: String,
        avatarKey: String?
    ): Result<Unit> {
        return model.updateProfile(username, displayName, avatarKey)
    }

    private fun applyUser(user: User?) {
        if (user == null) return

        username = user.username
        displayName = user.displayName
        avatarKey = user.avatarUrl
        maxXp = XP_PER_LEVEL

        val totalXp = user.xpTotal
        level = (totalXp / XP_PER_LEVEL) + 1
        val previousLevelsXp = (level - 1) * XP_PER_LEVEL
        currentXp = (totalXp - previousLevelsXp).coerceIn(0, XP_PER_LEVEL)

        joinedText = formatJoined(user.createdAtIso)
    }

    private fun applyStreak(streakData: com.example.gotouchgrass.domain.StreakData?) {
        if (streakData == null) return
        streakDays = streakData.currentDays
    }

    private fun applyLifetimeStats(stats: com.example.gotouchgrass.domain.LifetimeStats?) {
        if (stats == null) return
        // Placeholder: we can map lifetime stats to additional profile fields in the future.
    }

    private fun applyWeeklySummary(summary: com.example.gotouchgrass.domain.WeeklySummary?) {
        if (summary == null) return
        zonesVisited = summary.zonesVisited.toString()
        timeExploredHours = summary.timeOutside
    }

    private fun applyFriends(friends: List<User>) {
        // extract the first character of each friend's display name as their initial
        friendInitials = friends.mapNotNull { friend ->
            friend.displayName.firstOrNull()?.toString()?.uppercase()
        }
    }

    private fun timeAgoFrom(isoString: String): String {
        return try {
            val then = Instant.parse(isoString)
            val now = Instant.now()
            val diffMs = now.toEpochMilli() - then.toEpochMilli()
            when {
                diffMs < MILLIS_PER_MINUTE -> "Just now"
                diffMs < MILLIS_PER_HOUR -> "${diffMs / MILLIS_PER_MINUTE}m ago"
                diffMs < MILLIS_PER_DAY -> "${diffMs / MILLIS_PER_HOUR}h ago"
                diffMs < MILLIS_PER_WEEK -> "${diffMs / MILLIS_PER_DAY}d ago"
                else -> "${diffMs / MILLIS_PER_WEEK}w ago"
            }
        } catch (_: Exception) {
            "Recently"
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
