package com.example.gotouchgrass.data

import com.example.gotouchgrass.data.supabase.ChallengeProgressRow
import com.example.gotouchgrass.data.supabase.ChallengeRow
import com.example.gotouchgrass.data.supabase.RouteRow
import com.example.gotouchgrass.data.supabase.SearchActivityInsert
import com.example.gotouchgrass.data.supabase.SupabaseDataSource
import com.example.gotouchgrass.data.supabase.UserSettingsUpsert
import com.example.gotouchgrass.domain.ChallengeTimeWindow
import com.example.gotouchgrass.domain.ChallengeType
import com.example.gotouchgrass.domain.ExploreChallengeItem
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.LeaderboardData
import com.example.gotouchgrass.domain.LifetimeStats
import com.example.gotouchgrass.domain.RouteDifficulty
import com.example.gotouchgrass.domain.RouteTheme
import com.example.gotouchgrass.domain.StreakData
import com.example.gotouchgrass.domain.User
import com.example.gotouchgrass.domain.UserPreferences
import com.example.gotouchgrass.domain.WeeklySummary
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GoTouchGrassRepository(
    private val dataSource: SupabaseDataSource
) {
    enum class SearchEventSource {
        RESULT,
        RECENT,
        TRENDING
    }

    suspend fun getUser(userId: String): Result<User?> = dataSource.getUserById(userId)

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

    suspend fun getRecentSearches(userId: String, limit: Int = 8): Result<List<String>> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId)
            ?: return@runCatching emptyList()

        dataSource.fetchRecentSearchActivity(userRow.id, limit * 3)
            .map { it.queryText.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(limit)
    }

    suspend fun getTrendingSearches(limit: Int = 5): Result<List<String>> = runCatching {
        val rows = dataSource.fetchRecentGlobalSearchActivity(300)
        val grouped = rows
            .map { it.queryText.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it.lowercase() }
            .eachCount()

        val canonicalByKey = rows
            .map { it.queryText.trim() }
            .filter { it.isNotBlank() }
            .associateBy { it.lowercase() }

        grouped.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { entry -> canonicalByKey[entry.key] ?: entry.key }
            .take(limit)
    }

    suspend fun recordSearchEvent(
        userId: String,
        queryText: String,
        selectedPlaceId: String?,
        selectedTitle: String?,
        source: SearchEventSource
    ): Result<Unit> = runCatching {
        val normalizedQuery = queryText.trim()
        if (normalizedQuery.isBlank()) return@runCatching

        val userRow = dataSource.getUserRowByAuthId(userId)
            ?: return@runCatching

        dataSource.insertSearchActivity(
            SearchActivityInsert(
                userId = userRow.id,
                queryText = normalizedQuery,
                selectedPlaceId = selectedPlaceId,
                selectedTitle = selectedTitle,
                source = source.name
            )
        )
    }

    suspend fun recordChallengeProgress(
        userId: String,
        challengeId: Long,
        incrementBy: Double = 1.0
    ): Result<Unit> = runCatching {
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

    suspend fun getUserSettings(userId: String): Result<UserPreferences> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId)
            ?: return@runCatching defaultPreferences()
        val settingsRow = dataSource.fetchUserSettings(userRow.id)
            ?: return@runCatching defaultPreferences()
        UserPreferences(
            notificationsEnabled = settingsRow.notificationsEnabled,
            soundEffectsEnabled = settingsRow.soundEffectsEnabled,
            darkModeEnabled = settingsRow.darkModeEnabled,
            locationServicesEnabled = settingsRow.locationServicesEnabled
        )
    }

    suspend fun saveUserSettings(userId: String, prefs: UserPreferences): Result<Unit> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching
        dataSource.upsertUserSettings(
            UserSettingsUpsert(
                userId = userRow.id,
                notificationsEnabled = prefs.notificationsEnabled,
                soundEffectsEnabled = prefs.soundEffectsEnabled,
                darkModeEnabled = prefs.darkModeEnabled,
                locationServicesEnabled = prefs.locationServicesEnabled
            )
        )
    }

    private fun defaultPreferences() = UserPreferences(
        notificationsEnabled = true,
        soundEffectsEnabled = true,
        darkModeEnabled = false,
        locationServicesEnabled = true
    )

    suspend fun getLifetimeStats(userId: String): Result<LifetimeStats> = runCatching {
        val user = dataSource.getUserById(userId).getOrThrow()
            ?: return@runCatching LifetimeStats(totalXp = 0, coinsEarned = 0, totalDistanceKm = 0f, citiesExplored = 0)
        LifetimeStats(
            totalXp = user.xpTotal,
            coinsEarned = 0,        // TODO: fetch from coins table when available
            totalDistanceKm = 0f,   // TODO: compute from visit_session table when available
            citiesExplored = 0      // TODO: compute from city_completion table when available
        )
    }

    suspend fun getStreakData(userId: String): Result<StreakData> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId)
            ?: return@runCatching StreakData(currentDays = 0, bestDays = 0)
        val row = dataSource.fetchStreakByType(userRow.id, "DAILY_EXPLORE")
            ?: return@runCatching StreakData(currentDays = 0, bestDays = 0)
        StreakData(currentDays = row.currentCount, bestDays = row.bestCount)
    }

    suspend fun getWeeklySummary(userId: String): Result<WeeklySummary> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId)
            ?: return@runCatching defaultWeeklySummary()

        val weekStartIso = LocalDate.now(ZoneOffset.UTC)
            .with(DayOfWeek.MONDAY)
            .atStartOfDay(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val sessions = dataSource.fetchWeeklyVisitSessions(userRow.id, weekStartIso)
        if (sessions.isEmpty()) return@runCatching defaultWeeklySummary()

        val totalSec = sessions.sumOf { it.durationSec }
        val totalHours = totalSec / 3600.0
        val timeOutside = if (totalHours < 1) "${totalSec / 60}m" else "%.1fh".format(totalHours)

        val zonesVisited = sessions.mapNotNull { it.zoneId }.distinct().size

        val dailySeconds = LongArray(7)
        sessions.forEach { session ->
            val day = runCatching {
                OffsetDateTime.parse(session.startedAt).dayOfWeek.value - 1
            }.getOrNull() ?: return@forEach
            dailySeconds[day] += session.durationSec
        }
        val maxSec = dailySeconds.max().takeIf { it > 0 } ?: 1L
        val dailyActivity = dailySeconds.map { (it.toFloat() / maxSec).coerceIn(0f, 1f) }

        WeeklySummary(
            timeOutside = timeOutside,
            zonesVisited = zonesVisited,
            xpEarned = 0,           // TODO: compute from xp_event table when available
            dailyActivity = dailyActivity
        )
    }

    private fun defaultWeeklySummary() = WeeklySummary(
        timeOutside = "0h",
        zonesVisited = 0,
        xpEarned = 0,
        dailyActivity = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
    )

    suspend fun getLeaderboard(currentUserId: String): Result<List<LeaderboardData>> = runCatching {
        val topUsers = dataSource.fetchLeaderboardUsers(20)
        val currentUserRow = dataSource.getUserRowByAuthId(currentUserId)

        val entries = topUsers.mapIndexed { index, userRow ->
            val isCurrentUser = userRow.authUserId == currentUserId
            LeaderboardData(
                rank = (index + 1).toString(),
                name = if (isCurrentUser) "You" else userRow.displayName,
                level = "Level ${userRow.level}",
                xp = "%,d XP".format(userRow.xpTotal),
                isGoldRank = index == 0,
                isCurrentUser = isCurrentUser
            )
        }.toMutableList()

        if (currentUserRow != null && topUsers.none { it.authUserId == currentUserId }) {
            entries.add(
                LeaderboardData(
                    rank = "...",
                    name = "You",
                    level = "Level ${currentUserRow.level}",
                    xp = "%,d XP".format(currentUserRow.xpTotal),
                    isGoldRank = false,
                    isCurrentUser = true
                )
            )
        }

        entries
    }
}
