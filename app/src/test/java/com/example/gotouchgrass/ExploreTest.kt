package com.example.gotouchgrass

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import com.example.gotouchgrass.domain.ExploreModel
import org.junit.Test

class ExploreTest {
    private val repository = FakeGoTouchGrassRepository()

    @Test
    fun getTotalXp_getsTotalXp() = runBlocking {
        val model = ExploreModel("user_you", repository)

        val totalXp = model.getTotalXp()

        assertEquals(12450, totalXp)
    }

    @Test
    fun getDailyChallenges_getsDailyChallenge() = runBlocking {
        val model = ExploreModel("user_you", repository)

        val dailyChallenges = model.getDailyChallenges()

        assertEquals(1, dailyChallenges.size)
        assertEquals("challenge_daily_new_uw_zone", dailyChallenges.first().id)
    }

    @Test
    fun getWeeklyChallenges_getsWeeklyChallenge() = runBlocking {
        val model = ExploreModel("user_you", repository)

        val weeklyChallenges = model.getWeeklyChallenges()

        assertEquals(2, weeklyChallenges.size)
        assertEquals("challenge_weekly_ring_road", weeklyChallenges.first().id)
    }

    @Test
    fun getCuratedRoutes_getsCuratedRoutes() = runBlocking {
        val model = ExploreModel("user_you", repository)

        val curatedRoutes = model.getCuratedRoutes()

        assertEquals(2, curatedRoutes.size)
        assertEquals("route_uw_coffee_trail", curatedRoutes.first().id)
    }

}