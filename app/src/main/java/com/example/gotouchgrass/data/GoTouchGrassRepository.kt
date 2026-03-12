package com.example.gotouchgrass.data

import com.example.gotouchgrass.data.supabase.ChallengeProgressRow
import com.example.gotouchgrass.data.supabase.ChallengeRow
import com.example.gotouchgrass.data.supabase.RouteRow
import com.example.gotouchgrass.data.supabase.SupabaseDataSource
import com.example.gotouchgrass.domain.ChallengeTimeWindow
import com.example.gotouchgrass.domain.ChallengeType
import com.example.gotouchgrass.domain.ExploreChallengeItem
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.RouteDifficulty
import com.example.gotouchgrass.domain.RouteTheme
import com.example.gotouchgrass.domain.User
import org.json.JSONObject

class GoTouchGrassRepository(
    private val dataSource: SupabaseDataSource
) {
    // User
    suspend fun getUser(userId: String): Result<User?> = dataSource.getUserById(userId)

    // Explore page

    suspend fun getTotalXp(userId: String): Result<Int> = runCatching {
        dataSource.getUserById(userId).getOrThrow()?.xpTotal ?: 0
    }

    suspend fun getChallenges(
        userId: String,
        timeWindow: ChallengeTimeWindow
    ): Result<List<ExploreChallengeItem>> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId)
            ?: return@runCatching emptyList()

        val challenges = dataSource.fetchChallenges(timeWindow.name)
        val progressMap = dataSource.fetchChallengeProgress(userRow.id)
            .associateBy { it.challengeId }

        challenges.map { ch -> ch.toExploreChallengeItem(progressMap[ch.id]) }
    }

    suspend fun getCuratedRoutes(): Result<List<ExploreRouteItem>> = runCatching {
        val routes = dataSource.fetchRoutes()
        val stopsByRouteId = dataSource.fetchAllRouteStops().groupBy { it.routeId }

        routes.map { route ->
            val stops = (stopsByRouteId[route.id] ?: emptyList()).sortedBy { it.orderIndex }
            val stopStrings = stops.mapIndexed { idx, stop ->
                if (!stop.hintText.isNullOrBlank()) "${idx + 1}. ${stop.hintText}"
                else "Stop ${idx + 1}"
            }
            route.toExploreRouteItem(zoneCount = stops.size, routeStops = stopStrings)
        }
    }

    private fun ChallengeRow.toExploreChallengeItem(progress: ChallengeProgressRow?): ExploreChallengeItem {
        val progressValue = progress?.progressValue ?: 0.0
        val targetValue = extractTargetValue(ruleConfigJson.toString()) ?: 1.0
        val progressFraction = (progressValue / targetValue).coerceIn(0.0, 1.0).toFloat()
        val type =
            runCatching { ChallengeType.valueOf(challengeType) }.getOrDefault(ChallengeType.VISIT)

        return ExploreChallengeItem(
            id = id.toString(),
            title = title,
            description = description,
            rewardXp = rewardXp.toInt(),
            progress = "${formatValue(progressValue)} / ${formatValue(targetValue)}",
            progressFraction = progressFraction,
            challengeType = type
        )
    }

    private fun RouteRow.toExploreRouteItem(
        zoneCount: Int,
        routeStops: List<String>
    ): ExploreRouteItem {
        val parsedTheme =
            runCatching { RouteTheme.valueOf(theme) }.getOrDefault(RouteTheme.CITY_HIGHLIGHTS)
        val parsedDifficulty =
            runCatching { RouteDifficulty.valueOf(difficulty) }.getOrDefault(RouteDifficulty.EASY)
        return ExploreRouteItem(
            id = id.toString(),
            title = title,
            zoneCount = zoneCount,
            hours = estimatedDurationMinutes / 60.0,
            description = parsedTheme.toRouteDescription(),
            difficulty = parsedDifficulty,
            theme = parsedTheme,
            routeStops = routeStops
        )
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
            RouteTheme.FOOD -> "Discover local food spots"
            RouteTheme.DRINKS -> "Find your next drink stop"
            RouteTheme.PARKS -> "Parks and green spaces nearby"
            RouteTheme.MURALS -> "Street art and mural highlights"
            RouteTheme.HIDDEN_STUDY_SPOTS -> "Quiet and hidden study places"
            RouteTheme.CITY_HIGHLIGHTS -> "Top city highlights to explore"
        }
    }
}
