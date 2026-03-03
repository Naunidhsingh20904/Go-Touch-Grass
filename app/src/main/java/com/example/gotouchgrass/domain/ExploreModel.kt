package com.example.gotouchgrass.domain

import org.json.JSONObject

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
    private val currentUserId: String
) {
    fun getTotalXp(): Int {
        return FakeData.users.firstOrNull { it.id == currentUserId }?.xpTotal ?: 0
    }

    fun getDailyChallenges(): List<ExploreChallengeItem> {
        return FakeData.challenges
            .filter { it.timeWindow == ChallengeTimeWindow.DAILY }
            .map { challenge -> toChallengeItem(challenge) }
    }

    fun getWeeklyChallenges(): List<ExploreChallengeItem> {
        return FakeData.challenges
            .filter { it.timeWindow == ChallengeTimeWindow.WEEKLY }
            .map { challenge -> toChallengeItem(challenge) }
    }

    fun getCuratedRoutes(): List<ExploreRouteItem> {
        return FakeData.routes.map { route -> toRouteItem(route) }
    }

    private fun toChallengeItem(challenge: Challenge): ExploreChallengeItem {
        val progress = FakeData.challengeProgress
            .firstOrNull { it.userId == currentUserId && it.challengeId == challenge.id }

        val targetValue = challenge.extractTargetValue() ?: 1.0
        val progressValue = progress?.progressValue ?: 0.0
        val progressFraction = (progressValue / targetValue)
            .coerceIn(0.0, 1.0)
            .toFloat()

        return ExploreChallengeItem(
            id = challenge.id,
            title = challenge.title,
            description = challenge.description,
            rewardXp = challenge.rewardXP,
            progress = "${formatValue(progressValue)} / ${formatValue(targetValue)}",
            progressFraction = progressFraction,
            challengeType = challenge.challengeType
        )
    }

    private fun toRouteItem(route: Route): ExploreRouteItem {
        val zoneCount = FakeData.routeStops
            .count { stop -> stop.routeId == route.id && stop.zoneId != null }

        val routeStops = FakeData.routeStops
            .filter { stop -> stop.routeId == route.id }
            .sortedBy { stop -> stop.orderIndex }
            .map { stop ->
                val landmarkName = stop.landmarkId?.let { landmarkId ->
                    FakeData.landmarks.firstOrNull { it.id == landmarkId }?.name
                }
                val zoneName = stop.zoneId?.let { zoneId ->
                    FakeData.zones.firstOrNull { it.id == zoneId }?.name
                }
                val stopName = landmarkName ?: zoneName ?: "Stop ${stop.orderIndex}"
                val hint = stop.hintText

                if (hint.isNullOrBlank()) {
                    "${stop.orderIndex}. $stopName"
                } else {
                    "${stop.orderIndex}. $stopName: $hint"
                }
            }

        return ExploreRouteItem(
            id = route.id,
            title = route.title,
            zoneCount = zoneCount,
            hours = route.estimatedDurationMinutes / 60.0,
            description = route.theme.toRouteDescription(),
            difficulty = route.difficulty,
            theme = route.theme,
            routeStops = routeStops
        )
    }

    private fun RouteTheme.toRouteDescription(): String {
        return when (this) {
            RouteTheme.FOOD -> "Discover local food spots"
            RouteTheme.DRINKS -> "Find your next drink stop"
            RouteTheme.PARKS -> "Parks and green spaces nearby"
            RouteTheme.MURALS -> "Street art and mural highlights"
            RouteTheme.HIDDEN_STUDY_SPOTS -> "Quiet and hidden study places"
            RouteTheme.CITY_HIGHLIGHTS -> "Top city highlights to explore"
        }
    }

    private fun Challenge.extractTargetValue(): Double? {
        return try {
            val json = JSONObject(ruleConfigJson)
            val targetKeys = listOf("uniqueZones", "distanceKm", "timeMinutes")

            for (key in targetKeys) {
                if (!json.has(key)) continue
                val value = json.optDouble(key, Double.NaN)
                if (value.isFinite() && value > 0.0) return value
            }

            null
        } catch (_: Exception) {
            null
        }
    }

    private fun formatValue(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
    }
}
