package com.example.gotouchgrass.ui.stats

data class WeeklySummary(
    val timeOutside: String,
    val zonesVisited: Int,
    val xpEarned: Int,
    // 7 values (Mon–Sun), each in [0, 1] relative to the week's max activity
    val dailyActivity: List<Float>
)

data class StreakData(
    val currentDays: Int,
    val bestDays: Int
)

data class LifetimeStats(
    val totalXp: Int,
    val coinsEarned: Int,
    val totalDistanceKm: Float,
    val citiesExplored: Int
)
