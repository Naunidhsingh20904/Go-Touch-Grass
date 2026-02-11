package com.example.gotouchgrass.ui.stats

import androidx.lifecycle.ViewModel

class StatsViewModel : ViewModel() {

    val timeOutside = "19.3h"
    val zonesVisited = "28"
    val xpEarned = "+2,450"

    val currentStreak = "7 Days"
    val bestStreak = "14 Days"

    val totalXP = "12,450"
    val coinsEarned = "2,340"
    val totalDistance = "127 km"
    val citiesExplored = "3"

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