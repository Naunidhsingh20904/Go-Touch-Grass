package com.example.gotouchgrass.domain

import com.example.gotouchgrass.data.MapRepository
import com.example.gotouchgrass.data.ProfileRepository

private const val XP_PER_LEVEL = 1000

data class MapHeaderStats(
    val level: Int,
    val currentXp: Int,
    val maxXp: Int,
    val xpToNextLevel: Int,
    val totalXp: Int,
    val streakDays: Int,
    val zonesVisited: Int,
    val timeOutsideLabel: String
)

/**
 * Domain‑level model for the Map page.
 *
 * - Uses [ProfileRepository] for user progression + weekly summary stats.
 * - Uses [MapRepository] for "nearby areas" content (currently curated routes).
 *
 * This is the layer we unit test with mock repositories (no live DB).
 */
class MapModel(
    private val currentUserId: String,
    private val profileRepository: ProfileRepository,
    private val mapRepository: MapRepository
) {
    suspend fun getHeaderStats(): MapHeaderStats {
        val user = profileRepository.getUser(currentUserId).getOrNull()
        val streak = profileRepository.getStreakData(currentUserId).getOrNull()
        val weekly = profileRepository.getWeeklySummary(currentUserId).getOrNull()

        val level = user?.level ?: 1
        val totalXp = user?.xpTotal ?: 0
        val previousLevelsXp = (level - 1) * XP_PER_LEVEL
        val currentXp = (totalXp - previousLevelsXp).coerceIn(0, XP_PER_LEVEL)
        val maxXp = XP_PER_LEVEL

        val zonesVisited = weekly?.zonesVisited ?: 0
        val timeOutside = weekly?.timeOutside ?: "0h"

        return MapHeaderStats(
            level = level,
            currentXp = currentXp,
            maxXp = maxXp,
            xpToNextLevel = (maxXp - currentXp).coerceAtLeast(0),
            totalXp = totalXp,
            streakDays = streak?.currentDays ?: 0,
            zonesVisited = zonesVisited,
            timeOutsideLabel = timeOutside
        )
    }

    suspend fun getNearbyRoutes(): List<ExploreRouteItem> {
        return mapRepository.getNearbyRoutes().getOrDefault(emptyList())
    }
}

