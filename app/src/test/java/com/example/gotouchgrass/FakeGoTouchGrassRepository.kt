package com.example.gotouchgrass

import com.example.gotouchgrass.data.ExploreRepository
import com.example.gotouchgrass.domain.ChallengeTimeWindow
import com.example.gotouchgrass.domain.ChallengeType
import com.example.gotouchgrass.domain.ExploreChallengeItem
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.RouteTheme
import org.json.JSONObject

// for testing
class FakeGoTouchGrassRepository : ExploreRepository {

    override suspend fun getTotalXp(userId: String): Result<Int> = runCatching {
        FakeData.users.find { it.id == userId }?.xpTotal ?: 0
    }

    override suspend fun getChallenges(
        userId: String,
        timeWindow: ChallengeTimeWindow
    ): Result<List<ExploreChallengeItem>> = runCatching {
        // Filter challenges by time window
        val challenges = FakeData.challenges.filter { it.timeWindow == timeWindow }

        // Get progress data for the user
        val progressMap = FakeData.challengeProgress
            .filter { it.userId == userId }
            .associateBy { it.challengeId }

        challenges.map { ch ->
            val progress = progressMap[ch.id]
            val progressValue = progress?.progressValue ?: 0.0
            val targetValue = extractTargetValue(ch.ruleConfigJson) ?: 1.0
            val progressFraction = (progressValue / targetValue).coerceIn(0.0, 1.0).toFloat()
            val challengeType = runCatching { ChallengeType.valueOf(ch.challengeType.name) }
                .getOrDefault(ChallengeType.VISIT)

            ExploreChallengeItem(
                id = ch.id,
                title = ch.title,
                description = ch.description,
                rewardXp = ch.rewardXP,
                progress = "${formatValue(progressValue)} / ${formatValue(targetValue)}",
                progressFraction = progressFraction,
                challengeType = challengeType
            )
        }
    }

    override suspend fun getCuratedRoutes(): Result<List<ExploreRouteItem>> = runCatching {
        val routes = FakeData.routes
        val stopsByRouteId = FakeData.routeStops.groupBy { it.routeId }

        routes.map { route ->
            val stops = (stopsByRouteId[route.id] ?: emptyList()).sortedBy { it.orderIndex }
            val stopStrings = stops.mapIndexed { idx, stop ->
                if (!stop.hintText.isNullOrBlank()) "${idx + 1}. ${stop.hintText}"
                else "Stop ${idx + 1}"
            }

            ExploreRouteItem(
                id = route.id,
                title = route.title,
                zoneCount = stops.size,
                hours = route.estimatedDurationMinutes / 60.0,
                description = route.theme.toRouteDescription(),
                difficulty = route.difficulty,
                theme = route.theme,
                routeStops = stopStrings
            )
        }
    }

    private fun extractTargetValue(ruleConfigJson: String): Double? {
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

    private fun RouteTheme.toRouteDescription(): String {
        return when (this) {
            RouteTheme.CITY_HIGHLIGHTS -> "Discover the best city highlights"
            RouteTheme.FOOD -> "A culinary adventure through local cuisine"
            RouteTheme.PARKS -> "Peaceful parks and natural areas"
            RouteTheme.MURALS -> "Explore street art and murals"
            RouteTheme.DRINKS -> "Discover great bars and cafes"
            RouteTheme.HIDDEN_STUDY_SPOTS -> "Find quiet places to focus"
        }
    }
}
