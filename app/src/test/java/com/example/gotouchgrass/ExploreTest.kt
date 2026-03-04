package com.example.gotouchgrass

import org.junit.Assert.*
import com.example.gotouchgrass.domain.ExploreModel
import org.junit.Test

class ExploreTest {
    @Test
    fun getTotalXp_getsTotalXp() {
        val model = ExploreModel("user_you")

        val totalXp = model.getTotalXp()

        assertEquals(12450, totalXp)
    }

    @Test
    fun getDailyChallenges_getsDailyChallenge() {
        val model = ExploreModel("user_you")

        val dailyChallenges = model.getDailyChallenges()

        assertEquals(1, dailyChallenges.size)
        assertEquals("challenge_daily_new_uw_zone", dailyChallenges.first().id)
    }

    @Test
    fun getWeeklyChallenges_getsWeeklyChallenge() {
        val model = ExploreModel("user_you")

        val weeklyChallenges = model.getWeeklyChallenges()

        assertEquals(2, weeklyChallenges.size)
        assertEquals("challenge_weekly_ring_road", weeklyChallenges.first().id)
    }

    @Test
    fun getCuratedRoutes_getsCuratedRoutes() {
        val model = ExploreModel("user_you")

        val curatedRoutes = model.getCuratedRoutes()

        assertEquals(2, curatedRoutes.size)
        assertEquals("route_uw_coffee_trail", curatedRoutes.first().id)
    }

}