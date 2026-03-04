package com.example.gotouchgrass

import com.example.gotouchgrass.ui.stats.StatsViewModel
import org.junit.Assert.*
import org.junit.Test

class StatsViewModelTest {

    private val viewModel = StatsViewModel()

    // --- WeeklySummary ---

    @Test
    fun weeklySummary_dailyActivity_hasExactlySevenEntries() {
        assertEquals(7, viewModel.weeklySummary.dailyActivity.size)
    }

    @Test
    fun weeklySummary_dailyActivity_allValuesInValidRange() {
        assertTrue(viewModel.weeklySummary.dailyActivity.all { it in 0f..1f })
    }

    @Test
    fun weeklySummary_zonesVisited_isPositive() {
        assertTrue(viewModel.weeklySummary.zonesVisited > 0)
    }

    @Test
    fun weeklySummary_xpEarned_isPositive() {
        assertTrue(viewModel.weeklySummary.xpEarned > 0)
    }

    // --- StreakData ---

    @Test
    fun streak_currentDays_doesNotExceedBestDays() {
        assertTrue(viewModel.streak.currentDays <= viewModel.streak.bestDays)
    }

    @Test
    fun streak_currentDays_isPositive() {
        assertTrue(viewModel.streak.currentDays > 0)
    }

    // --- LifetimeStats ---

    @Test
    fun lifetimeStats_totalXp_isPositive() {
        assertTrue(viewModel.lifetimeStats.totalXp > 0)
    }

    @Test
    fun lifetimeStats_totalDistanceKm_isPositive() {
        assertTrue(viewModel.lifetimeStats.totalDistanceKm > 0f)
    }

    @Test
    fun lifetimeStats_citiesExplored_isPositive() {
        assertTrue(viewModel.lifetimeStats.citiesExplored > 0)
    }

    // --- Leaderboard ---

    @Test
    fun leaderboard_containsUserEntry() {
        assertTrue(viewModel.leaderboardEntries.any { it.name == "You" })
    }

    @Test
    fun leaderboard_isNotEmpty() {
        assertTrue(viewModel.leaderboardEntries.isNotEmpty())
    }

    @Test
    fun leaderboard_hasAtMostOneGoldRank() {
        val goldCount = viewModel.leaderboardEntries.count { it.isGoldRank }
        assertTrue(goldCount <= 1)
    }
}