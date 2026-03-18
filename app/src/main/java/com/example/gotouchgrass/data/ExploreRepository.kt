package com.example.gotouchgrass.data

import com.example.gotouchgrass.domain.ChallengeTimeWindow
import com.example.gotouchgrass.domain.ExploreChallengeItem
import com.example.gotouchgrass.domain.ExploreRouteItem

// for testing
interface ExploreRepository {
    suspend fun getTotalXp(userId: String): Result<Int>
    suspend fun getChallenges(
        userId: String,
        timeWindow: ChallengeTimeWindow
    ): Result<List<ExploreChallengeItem>>
    suspend fun getCuratedRoutes(): Result<List<ExploreRouteItem>>
}
