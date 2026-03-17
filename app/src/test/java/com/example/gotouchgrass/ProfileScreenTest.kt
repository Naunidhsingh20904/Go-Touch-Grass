package com.example.gotouchgrass

import com.example.gotouchgrass.data.FakeProfileRepository
import com.example.gotouchgrass.domain.ProfileModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Profile "model" layer using a mock DB.
 *
 * These tests exercise [ProfileModel] backed by [FakeProfileRepository],
 * so they never depend on live Supabase data.
 */
class ProfileScreenTest {

    private val repository = FakeProfileRepository()
    private val model = ProfileModel(
        currentUserId = "user_you",
        repository = repository
    )

    @Test
    fun model_username_comesFromMockDb() = runBlocking {
        val user = model.getUser()
        assertNotNull(user)
        assertEquals("uw_grasswalker", user!!.username)
    }

    @Test
    fun model_joinedText_canBeFormattedFromUserCreatedAt() = runBlocking {
        val user = model.getUser()
        assertNotNull(user)
        assertTrue(user!!.createdAtIso.isNotBlank())
    }

    @Test
    fun model_streakData_matchesMockStreak() = runBlocking {
        val streak = model.getStreakData()
        assertNotNull(streak)
        assertEquals(7, streak!!.currentDays)
    }

    @Test
    fun model_lifetimeStats_returnsTotalXp() = runBlocking {
        val stats = model.getLifetimeStats()
        assertNotNull(stats)
        // From FakeData the current user has 12450 XP.
        assertEquals(12450, stats!!.totalXp)
    }

    @Test
    fun model_weeklySummary_matchesMockValues() = runBlocking {
        val summary = model.getWeeklySummary()
        assertNotNull(summary)
        assertEquals("4h", summary!!.timeOutside)
        assertEquals(5, summary.zonesVisited)
        assertEquals(7, summary.dailyActivity.size)
    }
}

