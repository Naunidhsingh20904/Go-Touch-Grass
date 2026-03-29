package com.example.gotouchgrass.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.domain.ProfileModel
import com.example.gotouchgrass.domain.User
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

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

    var username by mutableStateOf("")
        private set

    var avatarKey by mutableStateOf<String?>(null)
        private set

    var joinedText by mutableStateOf("")
        private set

    var streakDays by mutableStateOf(0)
        private set

    var level by mutableStateOf(1)
        private set

    var currentXp by mutableStateOf(0)
        private set

    var maxXp by mutableStateOf(XP_PER_LEVEL)
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

                applyUser(user)
                applyStreak(streakData)
                applyLifetimeStats(lifetimeStats)
                applyWeeklySummary(weeklySummary)
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to load profile data"
            }

            isLoading = false
        }
    }

    suspend fun updateProfile(username: String, avatarKey: String?): Result<Unit> {
        return model.updateProfile(username, avatarKey)
    }

    private fun applyUser(user: User?) {
        if (user == null) return

        username = user.username
        avatarKey = user.avatarUrl
        level = user.level
        maxXp = XP_PER_LEVEL

        val totalXp = user.xpTotal
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
