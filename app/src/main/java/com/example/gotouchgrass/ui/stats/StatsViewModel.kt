package com.example.gotouchgrass.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.LeaderboardData
import com.example.gotouchgrass.domain.LifetimeStats
import com.example.gotouchgrass.domain.StreakData
import com.example.gotouchgrass.domain.StreakType
import com.example.gotouchgrass.domain.WeeklySummary
import kotlinx.coroutines.launch

class StatsViewModel(
    private val userId: String? = null,
    private val repository: GoTouchGrassRepository? = null
) : ViewModel() {

    var lifetimeStats by mutableStateOf(
        FakeData.users.firstOrNull()?.let { user ->
            LifetimeStats(
                totalXp = user.xpTotal,
                totalDistanceKm = 127f,
                citiesExplored = 3
            )
        } ?: LifetimeStats(totalXp = 0, totalDistanceKm = 0f, citiesExplored = 0)
    )
        private set

    var leaderboardEntries by mutableStateOf(
        listOf(
            LeaderboardData("1", "WorldExplorer", "Level 42", "125,000 XP", isGoldRank = true),
            LeaderboardData("2", "CityNomad", "Level 38", "98,500 XP", isGoldRank = false),
            LeaderboardData("3", "AdventureSeeker", "Level 35", "87,200 XP", isGoldRank = false),
            LeaderboardData("142", "You", "Level 8", "12,450 XP", isGoldRank = false, isCurrentUser = true)
        )
    )
        private set

    var isLoadingLeaderboard by mutableStateOf(false)
        private set

    var streak by mutableStateOf(
        FakeData.streaks.firstOrNull { it.type == StreakType.DAILY_EXPLORE }
            ?.let { StreakData(currentDays = it.currentCount, bestDays = it.bestCount) }
            ?: StreakData(currentDays = 0, bestDays = 0)
    )
        private set

    var weeklySummary by mutableStateOf(
        WeeklySummary(
            timeOutside = "19.3h",
            zonesVisited = 28,
            xpEarned = 2450,
            dailyActivity = listOf(0.84f, 0.63f, 0.95f, 0.53f, 0.74f, 1.0f, 0.79f)
        )
    )
        private set

    init {
        loadStats()
    }

    private fun loadStats() {
        val uid = userId ?: return
        val repo = repository ?: return
        viewModelScope.launch {
            repo.getLifetimeStats(uid).onSuccess { stats ->
                if (stats.totalXp > 0) lifetimeStats = stats
            }
            repo.getStreakData(uid).onSuccess { s ->
                if (s.currentDays > 0) streak = s
            }
            repo.getWeeklySummary(uid).onSuccess { summary ->
                if (summary.zonesVisited > 0) weeklySummary = summary
            }
            isLoadingLeaderboard = true
            repo.getLeaderboard(uid).onSuccess { entries ->
                if (entries.isNotEmpty()) leaderboardEntries = entries
            }
            isLoadingLeaderboard = false
        }
    }
}
