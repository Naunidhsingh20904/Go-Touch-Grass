package com.example.gotouchgrass

import com.example.gotouchgrass.ui.screens.ProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Profile / Milestones screen backed by [ProfileViewModel]
 * and [com.example.gotouchgrass.domain.FakeData].
 */
class ProfileScreenTest {

    private val viewModel = ProfileViewModel()

    @Test
    fun viewModel_username_comesFromFakeData() {
        assertEquals("uw_grasswalker", viewModel.username)
    }

    @Test
    fun viewModel_joinedText_isNonEmpty() {
        assertTrue(viewModel.joinedText.startsWith("Joined "))
        assertTrue(viewModel.joinedText.endsWith(" ago") || viewModel.joinedText == "Joined recently")
    }

    @Test
    fun viewModel_streakDays_matchesFakeDataDailyExploreStreak() {
        assertEquals(7, viewModel.streakDays)
    }

    @Test
    fun viewModel_level_matchesFakeDataUser() {
        assertEquals(8, viewModel.level)
    }

    @Test
    fun viewModel_currentXpAndMaxXp_areWithinLevelRange() {
        assertTrue(viewModel.currentXp in 0..viewModel.maxXp)
        assertEquals(1000, viewModel.maxXp)
    }

    @Test
    fun viewModel_zonesVisited_matchesFakeDataCityCompletion() {
        assertEquals("5", viewModel.zonesVisited)
    }

    @Test
    fun viewModel_zonesOwned_matchesFakeDataZoneOwnership() {
        assertEquals("1", viewModel.zonesOwned)
    }

    @Test
    fun viewModel_timeExplored_isNonEmpty() {
        assertTrue(viewModel.timeExploredHours.isNotEmpty())
    }

    @Test
    fun viewModel_badges_containsEarnedAndLockedFromFakeData() {
        val badges = viewModel.badges
        assertTrue(badges.isNotEmpty())
        val earned = badges.filter { it.isUnlocked }
        val locked = badges.filter { !it.isUnlocked }
        assertEquals(1, earned.size)
        assertTrue(locked.size >= 1)
        assertEquals("DC Pioneer", earned.first().name)
    }

    @Test
    fun viewModel_milestoneProgress_matchesFakeData() {
        assertNotNull(viewModel.milestoneProgressValue)
        assertEquals(4.0, viewModel.milestoneProgressValue!!, 0.0)
        assertTrue(viewModel.milestoneProgressList.isNotEmpty())
        assertEquals("milestone_unique_uw_zones_10", viewModel.milestoneProgressList.first().first)
        assertEquals(4.0, viewModel.milestoneProgressList.first().second, 0.0)
    }

    @Test
    fun viewModel_recentActivity_derivedFromXpEvents() {
        val activity = viewModel.recentActivity
        assertTrue(activity.isNotEmpty())
        activity.forEach { item ->
            assertTrue(item.name.isNotEmpty())
            assertTrue(item.xpText.startsWith("+") && item.xpText.endsWith(" XP"))
        }
    }

    @Test
    fun viewModel_friendInitials_derivedFromFakeDataFriendships() {
        val initials = viewModel.friendInitials
        assertEquals(1, initials.size)
        assertEquals("W", initials.first())
    }

    @Test
    fun viewModel_challengesDone_isString() {
        assertTrue(viewModel.challengesDone.toIntOrNull() != null || viewModel.challengesDone == "0")
    }
}
