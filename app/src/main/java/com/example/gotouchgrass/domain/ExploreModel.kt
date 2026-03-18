package com.example.gotouchgrass.domain

import com.example.gotouchgrass.data.ExploreRepository

data class ExploreChallengeItem(
    val id: String,
    val title: String,
    val description: String,
    val rewardXp: Int,
    val progress: String,
    val progressFraction: Float,
    val challengeType: ChallengeType
)

data class ExploreRouteItem(
    val id: String,
    val title: String,
    val zoneCount: Int,
    val hours: Double,
    val description: String,
    val difficulty: RouteDifficulty,
    val theme: RouteTheme,
    val routeStops: List<String>
)

class ExploreModel(
    private val currentUserId: String,
    private val repository: ExploreRepository
) {
    suspend fun getTotalXp(): Int = repository.getTotalXp(currentUserId).getOrDefault(0)

    suspend fun getDailyChallenges(): List<ExploreChallengeItem> =
        repository.getChallenges(currentUserId, ChallengeTimeWindow.DAILY).getOrDefault(emptyList())

    suspend fun getWeeklyChallenges(): List<ExploreChallengeItem> =
        repository.getChallenges(currentUserId, ChallengeTimeWindow.WEEKLY).getOrDefault(emptyList())

    suspend fun getCuratedRoutes(): List<ExploreRouteItem> =
        repository.getCuratedRoutes().getOrDefault(emptyList())
}
