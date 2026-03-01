package com.example.gotouchgrass.ui.stats

import androidx.lifecycle.ViewModel

class StatsViewModel : ViewModel() {

    val weeklySummary = WeeklySummary(
        timeOutside = "19.3h",
        zonesVisited = 28,
        xpEarned = 2450,
        dailyActivity = listOf(0.84f, 0.63f, 0.95f, 0.53f, 0.74f, 1.0f, 0.79f)
    )

    val streak = StreakData(currentDays = 7, bestDays = 14)

    val lifetimeStats = LifetimeStats(
        totalXp = 12450,
        coinsEarned = 2340,
        totalDistanceKm = 127f,
        citiesExplored = 3
    )

    val leaderboardEntries = listOf(
        LeaderboardData("1", "WorldExplorer", "Level 42", "125,000 XP", true),
        LeaderboardData("2", "CityNomad", "Level 38", "98,500 XP", false),
        LeaderboardData("3", "AdventureSeeker", "Level 35", "87,200 XP", false),
        LeaderboardData("142", "You", "Level 8", "12,450 XP", false)
    )
}

data class LeaderboardData(
    val rank: String,
    val name: String,
    val level: String,
    val xp: String,
    val isGoldRank: Boolean
)

