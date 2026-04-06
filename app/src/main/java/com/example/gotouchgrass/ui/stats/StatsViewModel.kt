package com.example.gotouchgrass.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.domain.LeaderboardData
import com.example.gotouchgrass.domain.LifetimeStats
import com.example.gotouchgrass.domain.StreakData
import com.example.gotouchgrass.domain.WeeklySummary
import kotlinx.coroutines.launch

class StatsViewModel(
    private val userId: String? = null,
    private val repository: GoTouchGrassRepository? = null
) : ViewModel() {

    var lifetimeStats by mutableStateOf(
        LifetimeStats(totalXp = 0, totalDistanceKm = 0f, citiesExplored = 0)
    )
        private set

    var leaderboardEntries by mutableStateOf<List<LeaderboardData>>(emptyList())
        private set

    var isLoadingLeaderboard by mutableStateOf(false)
        private set

    var streak by mutableStateOf(StreakData(currentDays = 0, bestDays = 0))
        private set

    var weeklySummary by mutableStateOf(
        WeeklySummary(
            timeOutside = "0h",
            zonesVisited = 0,
            xpEarned = 0,
            dailyActivity = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
    )
        private set

    var totalLandmarksCaptured by mutableIntStateOf(0)
        private set

    init {
        loadStats()
    }

    fun refresh() {
        loadStats()
    }

    private fun loadStats() {
        val uid = userId ?: return
        val repo = repository ?: return
        viewModelScope.launch {
            repo.getLifetimeStats(uid).onSuccess { stats ->
                lifetimeStats = stats
            }
            repo.getStreakData(uid).onSuccess { s ->
                streak = s
            }
            repo.getWeeklySummary(uid).onSuccess { summary ->
                weeklySummary = summary
            }
            repo.getTotalCapturedLandmarks(uid).onSuccess { total ->
                totalLandmarksCaptured = total
            }
            isLoadingLeaderboard = true
            repo.getLeaderboard(uid).onSuccess { entries ->
                leaderboardEntries = entries
            }
            isLoadingLeaderboard = false
        }
    }
}
