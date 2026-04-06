package com.example.gotouchgrass.data

import com.example.gotouchgrass.data.supabase.ChallengeProgressRow
import com.example.gotouchgrass.data.supabase.FriendRequestRow
import com.example.gotouchgrass.data.supabase.LandmarkInsert
import com.example.gotouchgrass.data.supabase.RouteRow
import com.example.gotouchgrass.data.supabase.SearchActivityInsert
import com.example.gotouchgrass.data.supabase.StreakUpsert
import com.example.gotouchgrass.data.supabase.SupabaseDataSource
import com.example.gotouchgrass.data.supabase.UserSettingsUpsert
import com.example.gotouchgrass.data.supabase.VisitSessionInsert
import com.example.gotouchgrass.domain.ChallengeTimeWindow
import com.example.gotouchgrass.domain.ChallengeType
import com.example.gotouchgrass.domain.CollectedLandmark
import com.example.gotouchgrass.domain.ExploreChallengeItem
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.FriendMapMarker
import com.example.gotouchgrass.domain.LatLng
import com.example.gotouchgrass.domain.LandmarkOwnershipSummary
import com.example.gotouchgrass.domain.LandmarkLeaderboardEntry
import com.example.gotouchgrass.domain.LeaderboardData
import com.example.gotouchgrass.domain.LifetimeStats
import com.example.gotouchgrass.domain.RouteDifficulty
import com.example.gotouchgrass.domain.RouteTheme
import com.example.gotouchgrass.domain.StreakData
import com.example.gotouchgrass.domain.TripZone
import com.example.gotouchgrass.domain.User
import com.example.gotouchgrass.domain.UserPreferences
import com.example.gotouchgrass.domain.WeeklySummary
import com.example.gotouchgrass.domain.isValidAvatarPresetKey
import com.example.gotouchgrass.domain.rarityScoreForLandmarkCategoryName
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import com.google.android.gms.maps.model.LatLng as GmsLatLng

open class GoTouchGrassRepository(
    private val dataSource: SupabaseDataSource
) : ExploreRepository {
    companion object {
        const val CONTEST_COOLDOWN_MINUTES = 30L
    }

    enum class SearchEventSource {
        RESULT, RECENT, TRENDING
    }

    private val firstCaptureXpAward = 120
    private val repeatContestXpAward = 30

    // add hard coded challenges for now instead of storing in and pulling from the db
    private data class HardcodedChallengeDefinition(
        val key: String,
        val title: String,
        val description: String,
        val challengeType: ChallengeType,
        val timeWindow: ChallengeTimeWindow,
        val targetValue: Double,
        val rewardXp: Int
    )

    private val hardcodedChallenges = listOf(
        HardcodedChallengeDefinition(
            key = "daily_capture_1",
            title = "Daily Scout",
            description = "Capture 1 new location today.",
            challengeType = ChallengeType.VISIT,
            timeWindow = ChallengeTimeWindow.DAILY,
            targetValue = 1.0,
            rewardXp = 100
        ),
        HardcodedChallengeDefinition(
            key = "weekly_trip_1",
            title = "Weekly Walker",
            description = "Complete 1 trip this week.",
            challengeType = ChallengeType.EXPLORE,
            timeWindow = ChallengeTimeWindow.WEEKLY,
            targetValue = 1.0,
            rewardXp = 200
        ),
        HardcodedChallengeDefinition(
            key = "weekly_time_60",
            title = "Weekly Outdoors",
            description = "Spend 60 minutes outdoors this week.",
            challengeType = ChallengeType.TIME,
            timeWindow = ChallengeTimeWindow.WEEKLY,
            targetValue = 60.0,
            rewardXp = 250
        ),
        HardcodedChallengeDefinition(
            key = "weekly_capture_5",
            title = "Weekly Collector",
            description = "Capture 5 new locations this week.",
            challengeType = ChallengeType.VISIT,
            timeWindow = ChallengeTimeWindow.WEEKLY,
            targetValue = 5.0,
            rewardXp = 300
        )
    )

    // User
    suspend fun getUser(userId: String): Result<User?> = dataSource.getUserById(userId)

    suspend fun updateUserProfile(
        userId: String,
        username: String,
        displayName: String,
        avatarKey: String?
    ): Result<Unit> = runCatching {
        val trimmedUsername = username.trim()
        val trimmedDisplayName = displayName.trim()
        require(trimmedUsername.isNotEmpty()) { "Username cannot be empty" }
        require(trimmedDisplayName.isNotEmpty()) { "Display name cannot be empty" }
        require(isValidAvatarPresetKey(avatarKey)) { "Invalid avatar preset" }
        dataSource.updateUserProfileByAuthId(userId, trimmedUsername, trimmedDisplayName, avatarKey)
    }

    // Explore page

    override suspend fun getTotalXp(userId: String): Result<Int> = runCatching {
        dataSource.getUserById(userId).getOrThrow()?.xpTotal ?: 0
    }

    override suspend fun getChallenges(
        userId: String, timeWindow: ChallengeTimeWindow
    ): Result<List<ExploreChallengeItem>> = runCatching {
        val userRow = runCatching { dataSource.getUserRowByAuthId(userId) }.getOrNull()
        val definitions = hardcodedChallenges.filter { it.timeWindow == timeWindow }
        val idByLookupKey = runCatching { resolveHardcodedChallengeIds() }.getOrDefault(emptyMap())
        val currentPeriodStartIso = currentChallengePeriodStartIso(timeWindow)
        val progressMap = if (userRow == null) {
            emptyMap()
        } else {
            runCatching {
                dataSource.fetchChallengeProgress(userRow.id)
                    .filter { it.periodStartIso == currentPeriodStartIso }
                    .associateBy { it.challengeId }
            }.getOrDefault(emptyMap())
        }
        val fallbackVisitProgress = if (userRow == null) {
            0.0
        } else {
            runCatching {
                countCapturesInPeriod(userRow.id, timeWindow).toDouble()
            }.getOrDefault(0.0)
        }

        definitions.map { definition ->
            val lookupKey = challengeLookupKey(definition.timeWindow, definition.title)
            val challengeId = idByLookupKey[lookupKey]
            val storedProgressValue = challengeId?.let { progressMap[it]?.progressValue } ?: 0.0
            val progressValue = if (
                definition.challengeType == ChallengeType.VISIT &&
                storedProgressValue <= 0.0
            ) {
                fallbackVisitProgress
            } else {
                storedProgressValue
            }
            val displayProgressValue = progressValue.coerceIn(0.0, definition.targetValue)
            val progressFraction =
                (displayProgressValue / definition.targetValue).coerceIn(0.0, 1.0).toFloat()

            ExploreChallengeItem(
                id = challengeId?.toString() ?: "hardcoded_${definition.key}",
                title = definition.title,
                description = definition.description,
                rewardXp = definition.rewardXp,
                progress = "${formatValue(displayProgressValue)} / ${formatValue(definition.targetValue)}",
                progressFraction = progressFraction,
                challengeType = definition.challengeType
            )
        }
    }

    suspend fun getRecentSearches(userId: String, limit: Int = 8): Result<List<String>> =
        runCatching {
            val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching emptyList()

            dataSource.fetchRecentSearchActivity(userRow.id, limit * 3).map { it.queryText.trim() }
                .filter { it.isNotBlank() }.distinctBy { it.lowercase() }.take(limit)
        }

    // function logic generated by Claude Sonnet 4.6
    suspend fun getTrendingSearches(limit: Int = 5): Result<List<String>> = runCatching {
        val rows = dataSource.fetchRecentGlobalSearchActivity(300)
        val grouped = rows.map { it.queryText.trim() }.filter { it.isNotBlank() }
            .groupingBy { it.lowercase() }.eachCount()

        val canonicalByKey = rows.map { it.queryText.trim() }.filter { it.isNotBlank() }
            .associateBy { it.lowercase() }

        grouped.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { entry -> canonicalByKey[entry.key] ?: entry.key }.take(limit)
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

        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching

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

    suspend fun recordCaptureByPlaceId(userId: String, placeId: String): Result<List<String>> =
        runCatching {
            val normalizedPlaceId = placeId.trim()
            if (normalizedPlaceId.isBlank()) return@runCatching emptyList()

            val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching emptyList()
            val landmark =
                dataSource.fetchLandmarkByPlaceId(normalizedPlaceId) ?: throw IllegalStateException(
                    "This location is not mapped to a landmark yet."
                )

            val alreadyCaptured = dataSource.hasCaptureForUserAndLandmark(userRow.id, landmark.id)
            if (alreadyCaptured) {
                val latestOwnershipEventAt = latestOwnershipEventForUserAndLandmark(userRow.id, landmark.id)
                val latestCaptureAt = latestOwnershipEventAt?.let { it }
                val remainingMinutes = latestCaptureAt?.let { contestCooldownRemainingMinutes(it) } ?: 0L
                if (remainingMinutes > 0L) {
                    throw IllegalStateException("Contest cooldown active. Try again in about ${remainingMinutes}m.")
                }
            }

            val xpAward = if (alreadyCaptured) repeatContestXpAward else firstCaptureXpAward

            if (alreadyCaptured) {
                dataSource.insertContest(
                    com.example.gotouchgrass.data.supabase.ContestInsert(
                        userId = userRow.id,
                        landmarkId = landmark.id,
                        xpAwarded = xpAward
                    )
                )
            } else {
                dataSource.insertCapture(
                    com.example.gotouchgrass.data.supabase.CaptureInsert(
                        userId = userRow.id,
                        zoneId = landmark.zoneId,
                        landmarkId = landmark.id,
                        rarityAtTime = rarityScoreForLandmarkCategoryName(landmark.category ?: "OTHER"),
                        xpAwarded = xpAward
                    )
                )
            }

            // update user xp total on capture
            val newXpTotal = userRow.xpTotal + xpAward.toLong()
            dataSource.updateUserXpTotal(userId = userRow.id, newXpTotal = newXpTotal)
            val newLevel = (newXpTotal / 1000).toInt() + 1
            if (newLevel != userRow.level.toInt()) {
                dataSource.updateUserLevel(userRow.id, newLevel)
            }

            if (!alreadyCaptured) {
                applyCaptureChallengeProgress(userId)
            } else {
                emptyList()
            }
        }

    suspend fun getMappedLandmarkCategoryForPlaceId(placeId: String): Result<String?> =
        runCatching {
            val normalizedPlaceId = placeId.trim()
            if (normalizedPlaceId.isBlank()) return@runCatching null
            dataSource.fetchLandmarkByPlaceId(normalizedPlaceId)?.category
        }

    suspend fun isPlaceMappedForCapture(placeId: String): Result<Boolean> = runCatching {
        val normalizedPlaceId = placeId.trim()
        if (normalizedPlaceId.isBlank()) return@runCatching false
        dataSource.fetchLandmarkByPlaceId(normalizedPlaceId) != null
    }

    suspend fun ensureLandmarkMappedForCapture(
        userId: String,
        placeId: String
    ): Result<String?> =
        runCatching {
            val normalizedPlaceId = placeId.trim()
            require(normalizedPlaceId.isNotBlank()) { "Place ID cannot be empty." }

            val existing = dataSource.fetchLandmarkByPlaceId(normalizedPlaceId)
            if (existing != null) {
                return@runCatching existing.category
            }

            val creatorUserId = dataSource.getUserRowByAuthId(userId)?.id
            val insertResult = runCatching {
                dataSource.insertLandmark(
                    LandmarkInsert(
                        placeId = normalizedPlaceId,
                        createdByUserId = creatorUserId
                    )
                )
            }

            if (insertResult.isFailure) {
                // might've been updated still so check if it exists
                val mappedAfterFailure = dataSource.fetchLandmarkByPlaceId(normalizedPlaceId)
                if (mappedAfterFailure != null) {
                    return@runCatching mappedAfterFailure.category
                }
                throw (insertResult.exceptionOrNull()
                    ?: IllegalStateException("Landmark mapping failed."))
            }

            dataSource.fetchLandmarkByPlaceId(normalizedPlaceId)?.category
        }

    suspend fun getLatestCaptureDateForPlaceId(
        userId: String, placeId: String
    ): Result<String?> = runCatching {
        val normalizedPlaceId = placeId.trim()
        if (normalizedPlaceId.isBlank()) return@runCatching null

        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching null
        val landmark =
            dataSource.fetchLandmarkByPlaceId(normalizedPlaceId) ?: return@runCatching null

        latestOwnershipEventForUserAndLandmark(userRow.id, landmark.id)
    }

    suspend fun getLandmarkOwnershipSummaryByPlaceId(
        placeId: String
    ): Result<LandmarkOwnershipSummary?> = runCatching {
        val normalizedPlaceId = placeId.trim()
        if (normalizedPlaceId.isBlank()) return@runCatching null

        val landmark = dataSource.fetchLandmarkByPlaceId(normalizedPlaceId) ?: return@runCatching null
        val firstDiscovererUser = landmark.createdByUserId?.let { dataSource.getUserRowById(it) }
        val latestCapture = dataSource.fetchLatestCaptureByLandmark(landmark.id)
        val latestContest = dataSource.fetchLatestContestByLandmark(landmark.id)
        val latestCaptureUser = latestCapture?.userId?.let { dataSource.getUserRowById(it) }
        val latestContestUser = latestContest?.userId?.let { dataSource.getUserRowById(it) }
        val captures = dataSource.fetchCapturesByLandmark(landmark.id)
        val contests = dataSource.fetchContestsByLandmark(landmark.id)

        val leaderEntry = buildLeaderBoardForLandmark(captures, contests)

        val leaderUser = leaderEntry?.first?.let { dataSource.getUserRowById(it) }

        LandmarkOwnershipSummary(
            placeId = landmark.placeId,
            firstDiscovererUserId = landmark.createdByUserId,
            firstDiscovererName = firstDiscovererUser?.displayName ?: firstDiscovererUser?.username,
            mostRecentCapturerUserId = latestContest?.userId ?: latestCapture?.userId,
            mostRecentCapturerName = latestContestUser?.displayName
                ?: latestContestUser?.username
                ?: latestCaptureUser?.displayName
                ?: latestCaptureUser?.username,
            mostRecentCaptureAtIso = latestContest?.createdAt ?: latestCapture?.capturedAt ?: latestCapture?.createdAt,
            currentLeaderUserId = leaderEntry?.first,
            currentLeaderName = leaderUser?.displayName ?: leaderUser?.username,
            currentLeaderContestScore = leaderEntry?.second ?: 0,
            currentLeaderLastContestAtIso = leaderEntry?.third
        )
    }

    suspend fun getLandmarkLeaderboardByPlaceId(
        placeId: String
    ): Result<List<LandmarkLeaderboardEntry>> = runCatching {
        val normalizedPlaceId = placeId.trim()
        if (normalizedPlaceId.isBlank()) return@runCatching emptyList()

        val landmark = dataSource.fetchLandmarkByPlaceId(normalizedPlaceId) ?: return@runCatching emptyList()
        val captures = dataSource.fetchCapturesByLandmark(landmark.id)
        val contests = dataSource.fetchContestsByLandmark(landmark.id)
        val leaderboardRows = buildLeaderboardRowsForLandmark(captures, contests)

        leaderboardRows.mapNotNull { row ->
            val user = dataSource.getUserRowById(row.userId) ?: return@mapNotNull null
            LandmarkLeaderboardEntry(
                userId = user.id,
                authUserId = user.authUserId,
                displayName = user.displayName.ifBlank { user.username },
                score = row.score,
                lastEventAtIso = row.latestEventAtIso
            )
        }
    }

    private fun contestCooldownRemainingMinutes(lastCaptureIso: String): Long {
        val lastCaptureAt = parseIso(lastCaptureIso) ?: return 0L
        val cooldownUntil = lastCaptureAt.plusMinutes(CONTEST_COOLDOWN_MINUTES)
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val remainingSeconds = java.time.Duration.between(now, cooldownUntil).seconds
        if (remainingSeconds <= 0) return 0L
        return ((remainingSeconds + 59) / 60)
    }

    private fun parseIso(value: String?): OffsetDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value) }.getOrNull()
    }

    private suspend fun latestOwnershipEventForUserAndLandmark(userId: Long, landmarkId: Long): String? {
        val latestCaptureAt = dataSource.fetchLatestCaptureByUserAndLandmark(userId, landmarkId)
            ?.let { it.capturedAt ?: it.createdAt }
        val latestContestAt = dataSource.fetchLatestContestByUserAndLandmark(userId, landmarkId)
            ?.createdAt
        return listOfNotNull(latestCaptureAt, latestContestAt)
            .maxWithOrNull(compareBy<String> { it })
    }

    private fun buildLeaderBoardForLandmark(
        captures: List<com.example.gotouchgrass.data.supabase.CaptureRow>,
        contests: List<com.example.gotouchgrass.data.supabase.ContestRow>
    ): Triple<Long, Int, String?>? {
        return buildLeaderboardRowsForLandmark(captures, contests).firstOrNull()?.let {
            Triple(it.userId, it.score, it.latestEventAtIso)
        }
    }

    private fun buildLeaderboardRowsForLandmark(
        captures: List<com.example.gotouchgrass.data.supabase.CaptureRow>,
        contests: List<com.example.gotouchgrass.data.supabase.ContestRow>
    ): List<LeaderboardScoreRow> {
        val captureScores = captures.groupBy { it.userId }.mapValues { entry ->
            val latestCaptureAt = entry.value.maxByOrNull { it.capturedAt ?: it.createdAt }
                ?.let { it.capturedAt ?: it.createdAt }
            ScoreBundle(score = 1, latestEventAtIso = latestCaptureAt)
        }

        val contestScores = contests.groupBy { it.userId }.mapValues { entry ->
            val latestContestAt = entry.value.maxByOrNull { it.createdAt }?.createdAt
            ScoreBundle(score = entry.value.size, latestEventAtIso = latestContestAt)
        }

        val allUserIds = (captureScores.keys + contestScores.keys).toSet()
        return allUserIds.mapNotNull { userId ->
            val captureScore = captureScores[userId]
            val contestScore = contestScores[userId]
            val totalScore = (captureScore?.score ?: 0) + (contestScore?.score ?: 0)
            if (totalScore <= 0) return@mapNotNull null
            val latestEventAtIso = listOfNotNull(captureScore?.latestEventAtIso, contestScore?.latestEventAtIso)
                .maxWithOrNull(compareBy<String> { it })
            LeaderboardScoreRow(userId = userId, score = totalScore, latestEventAtIso = latestEventAtIso)
        }.sortedWith(
            compareByDescending<LeaderboardScoreRow> { it.score }
                .thenByDescending { it.latestEventAtIso ?: "" }
                .thenBy { it.userId }
        )
    }

    private data class ScoreBundle(
        val score: Int,
        val latestEventAtIso: String?
    )

    private data class LeaderboardScoreRow(
        val userId: Long,
        val score: Int,
        val latestEventAtIso: String?
    )

    suspend fun getCapturedPlaceIdsByUserId(userId: String): Result<Set<String>> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching emptySet()

        val capturedLandmarkIds = dataSource.fetchCapturedLandmarkIdsByUser(userRow.id)
        if (capturedLandmarkIds.isEmpty()) return@runCatching emptySet()

        dataSource.fetchLandmarksByIds(capturedLandmarkIds).map { it.placeId }.toSet()
    }

    suspend fun getCollectedLandmarks(userId: String): Result<List<CollectedLandmark>> =
        runCatching {
            val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching emptyList()
            val captures = dataSource.fetchCapturesByUser(userRow.id)
                .filter { it.landmarkId != null && !it.capturedAt.isNullOrBlank() }

            if (captures.isEmpty()) return@runCatching emptyList()

            val landmarkIds = captures.mapNotNull { it.landmarkId }.distinct()
            val landmarksById = dataSource.fetchLandmarksByIds(landmarkIds).associateBy { it.id }

            captures.mapNotNull { capture ->
                val landmarkId = capture.landmarkId ?: return@mapNotNull null
                val capturedAt = capture.capturedAt ?: return@mapNotNull null
                val landmark = landmarksById[landmarkId] ?: return@mapNotNull null
                CollectedLandmark(
                    landmarkId = landmarkId,
                    placeId = landmark.placeId,
                    category = landmark.category ?: "OTHER",
                    capturedAtIso = capturedAt
                )
            }
        }

    suspend fun getTotalCapturedLandmarks(userId: String): Result<Int> = runCatching {
        getCollectedLandmarks(userId).getOrThrow().size
    }

    suspend fun getRecentActivity(userId: String, limit: Int = 10): Result<List<com.example.gotouchgrass.domain.RecentActivity>> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching emptyList()
        val captures = dataSource.fetchCapturesByUser(userRow.id)
            .filter { !it.capturedAt.isNullOrBlank() }
            .take(limit)

        if (captures.isEmpty()) return@runCatching emptyList()

        val landmarkIds = captures.mapNotNull { it.landmarkId }.distinct()
        val landmarksById = if (landmarkIds.isNotEmpty())
            dataSource.fetchLandmarksByIds(landmarkIds).associateBy { it.id }
        else emptyMap()

        captures.mapNotNull { capture ->
            val capturedAt = capture.capturedAt ?: return@mapNotNull null
            val landmark = capture.landmarkId?.let { landmarksById[it] }
            val category = landmark?.category ?: "OTHER"
            com.example.gotouchgrass.domain.RecentActivity(
                displayName = categoryToActivityDisplayName(category),
                capturedAtIso = capturedAt,
                xpAwarded = capture.xpAwarded
            )
        }
    }

    private fun categoryToActivityDisplayName(category: String): String {
        return when (category.uppercase()) {
            "PARK" -> "Explored a Park"
            "STUDY_SPOT" -> "Study Spot Captured"
            "CAFE" -> "Visited a Café"
            "FOOD" -> "Found a Food Spot"
            "TRANSPORTATION" -> "Transit Point Found"
            "EDUCATION" -> "Campus Discovery"
            "GYM" -> "Gym Captured"
            "LOUNGE" -> "Lounge Discovered"
            "MURAL" -> "Mural Found"
            "STATUE" -> "Statue Discovered"
            else -> "Landmark Captured"
        }
    }

    // call this from real game events (zone capture, location visit, etc.)
    suspend fun recordChallengeProgress(
        userId: String, challengeId: Long, incrementBy: Double = 1.0
    ): Result<Boolean> = runCatching {
        if (incrementBy <= 0.0) return@runCatching false

        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching false
        val definition = resolveHardcodedDefinitionById(challengeId) ?: return@runCatching false
        val periodStartIso = currentChallengePeriodStartIso(definition.timeWindow)

        val existingProgress = dataSource.fetchChallengeProgress(userRow.id)
            .firstOrNull { it.challengeId == challengeId && it.periodStartIso == periodStartIso }

        if (!existingProgress?.completedAt.isNullOrBlank()) return@runCatching false

        val oldValue = existingProgress?.progressValue ?: 0.0
        val newValue = (oldValue + incrementBy).coerceAtMost(definition.targetValue)
        val completedNow = oldValue < definition.targetValue && newValue >= definition.targetValue
        val nowIso = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        dataSource.upsertChallengeProgress(
            ChallengeProgressRow(
                userId = userRow.id,
                challengeId = challengeId,
                periodStartIso = periodStartIso,
                progressValue = newValue,
                completedAt = if (completedNow) nowIso else existingProgress?.completedAt
            )
        )

        if (!completedNow) return@runCatching false

        val didInsertAward = dataSource.tryInsertChallengeXpAward(
            com.example.gotouchgrass.data.supabase.ChallengeXpAwardInsert(
                userId = userRow.id,
                challengeId = challengeId,
                periodStartIso = periodStartIso,
                awardedXp = definition.rewardXp.toLong()
            )
        )
        if (!didInsertAward) return@runCatching false

        val updatedXpTotal = userRow.xpTotal + definition.rewardXp
        dataSource.updateUserXpTotal(userId = userRow.id, newXpTotal = updatedXpTotal)
        val updatedLevel = (updatedXpTotal / 1000).toInt() + 1
        if (updatedLevel != userRow.level.toInt()) {
            dataSource.updateUserLevel(userRow.id, updatedLevel)
        }
        true
    }

    override suspend fun getCuratedRoutes(): Result<List<ExploreRouteItem>> = runCatching {
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

    private fun RouteRow.toExploreRouteItem(
        zoneCount: Int, routeStops: List<String>
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

    private fun formatValue(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
    }

    private fun currentChallengePeriodStartIso(
        timeWindow: ChallengeTimeWindow,
        nowDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val periodStart = when (timeWindow) {
            ChallengeTimeWindow.DAILY -> nowDate
            ChallengeTimeWindow.WEEKLY -> nowDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
        return periodStart.atStartOfDay(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    // for tracking challenged, function generated by GPT-5.3-Codex
    private suspend fun countCapturesInPeriod(userId: Long, timeWindow: ChallengeTimeWindow): Int {
        val captures = dataSource.fetchCapturesByUser(userId)
        val today = LocalDate.now(ZoneId.systemDefault())
        return captures.count { capture ->
            val timestampIso = capture.capturedAt ?: capture.createdAt
            val capturedDate = runCatching {
                OffsetDateTime.parse(timestampIso).atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate()
            }.getOrNull() ?: return@count false

            when (timeWindow) {
                ChallengeTimeWindow.DAILY -> capturedDate == today
                ChallengeTimeWindow.WEEKLY -> {
                    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val weekEnd = weekStart.plusDays(6)
                    !capturedDate.isBefore(weekStart) && !capturedDate.isAfter(weekEnd)
                }
            }
        }
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

    private suspend fun resolveHardcodedChallengeIds(): Map<String, Long> {
        val dailyRows = runCatching { dataSource.fetchChallenges(ChallengeTimeWindow.DAILY.name) }
            .getOrDefault(emptyList())
        val weeklyRows = runCatching { dataSource.fetchChallenges(ChallengeTimeWindow.WEEKLY.name) }
            .getOrDefault(emptyList())
        val rows = dailyRows + weeklyRows

        return hardcodedChallenges.mapNotNull { definition ->
            val matched = rows.firstOrNull { row ->
                row.timeWindow.equals(definition.timeWindow.name, ignoreCase = true) &&
                        row.title.equals(definition.title, ignoreCase = true)
            } ?: return@mapNotNull null

            challengeLookupKey(definition.timeWindow, definition.title) to matched.id
        }.toMap()
    }

    private suspend fun resolveHardcodedDefinitionById(challengeId: Long): HardcodedChallengeDefinition? {
        val idByLookupKey = resolveHardcodedChallengeIds()
        val lookupKey =
            idByLookupKey.entries.firstOrNull { it.value == challengeId }?.key ?: return null
        return hardcodedChallenges.firstOrNull {
            challengeLookupKey(
                it.timeWindow,
                it.title
            ) == lookupKey
        }
    }

    private fun challengeLookupKey(timeWindow: ChallengeTimeWindow, title: String): String {
        return "${timeWindow.name}:${title.lowercase()}"
    }

    suspend fun getUserSettings(userId: String): Result<UserPreferences> = runCatching {
        val userRow =
            dataSource.getUserRowByAuthId(userId) ?: return@runCatching defaultPreferences()
        val settingsRow =
            dataSource.fetchUserSettings(userRow.id) ?: return@runCatching defaultPreferences()
        UserPreferences(
            notificationsEnabled = settingsRow.notificationsEnabled,
            soundEffectsEnabled = settingsRow.soundEffectsEnabled,
            darkModeEnabled = settingsRow.darkModeEnabled,
            locationServicesEnabled = settingsRow.locationServicesEnabled
        )
    }

    suspend fun saveUserSettings(userId: String, prefs: UserPreferences): Result<Unit> =
        runCatching {
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
        val user = dataSource.getUserById(userId).getOrThrow() ?: return@runCatching LifetimeStats(
            totalXp = 0, totalDistanceKm = 0f, citiesExplored = 0
        )
        LifetimeStats(
            totalXp = user.xpTotal,
            totalDistanceKm = 0f,   // TODO: compute from visit_session table when available
            citiesExplored = 0      // TODO: compute from city_completion table when available
        )
    }

    suspend fun getStreakData(userId: String): Result<StreakData> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching StreakData(
            currentDays = 0,
            bestDays = 0
        )
        val row = dataSource.fetchStreakByType(userRow.id, "DAILY_EXPLORE")
            ?: return@runCatching StreakData(currentDays = 0, bestDays = 0)
        StreakData(currentDays = row.currentCount, bestDays = row.bestCount)
    }

    suspend fun getWeeklySummary(userId: String): Result<WeeklySummary> = runCatching {
        val userRow =
            dataSource.getUserRowByAuthId(userId) ?: return@runCatching defaultWeeklySummary()

        val weekStartIso =
            LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY).atStartOfDay(ZoneOffset.UTC)
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

        val weeklyCaptures = dataSource.fetchWeeklyCaptures(userRow.id, weekStartIso)
        val weeklyXp = weeklyCaptures.sumOf { it.xpAwarded }

        WeeklySummary(
            timeOutside = timeOutside,
            zonesVisited = zonesVisited,
            xpEarned = weeklyXp,
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

        val XP_PER_LEVEL = 1000

        val entries = topUsers.mapIndexed { index, userRow ->
            val isCurrentUser = userRow.authUserId == currentUserId
            val derivedLevel = (userRow.xpTotal / XP_PER_LEVEL) + 1
            LeaderboardData(
                rank = (index + 1).toString(),
                name = if (isCurrentUser) "You" else userRow.displayName,
                level = "Level $derivedLevel",
                xp = "%,d XP".format(userRow.xpTotal),
                isGoldRank = index == 0,
                isCurrentUser = isCurrentUser
            )
        }.toMutableList()

        if (currentUserRow != null && topUsers.none { it.authUserId == currentUserId }) {
            val derivedLevel = (currentUserRow.xpTotal / XP_PER_LEVEL) + 1
            entries.add(
                LeaderboardData(
                    rank = "...",
                    name = "You",
                    level = "Level $derivedLevel",
                    xp = "%,d XP".format(currentUserRow.xpTotal),
                    isGoldRank = false,
                    isCurrentUser = true
                )
            )
        }

        entries
    }

    // ── Trip / visit session ──────────────────────────────────────────────────

    suspend fun recordVisitSession(
        userId: String,
        startedAtIso: String,
        endedAtIso: String,
        durationSec: Long,
        dominantZoneId: Long?
    ): Result<List<String>> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching emptyList()
        dataSource.insertVisitSession(
            VisitSessionInsert(
                userId = userRow.id,
                zoneId = dominantZoneId,
                startedAt = startedAtIso,
                endedAt = endedAtIso,
                durationSec = durationSec,
                source = "AUTO"
            )
        )

        applyTripEndChallengeProgress(
            userId = userId,
            tripDurationSec = durationSec
        )
    }

    private suspend fun applyCaptureChallengeProgress(userId: String): List<String> {
        val completedChallengeTitles = mutableListOf<String>()
        val idByLookupKey = resolveHardcodedChallengeIds()
        val captureChallenges = hardcodedChallenges.filter {
            it.challengeType == ChallengeType.VISIT &&
                    (it.key == "daily_capture_1" || it.key == "weekly_capture_5")
        }

        captureChallenges.forEach { definition ->
            val challengeId =
                idByLookupKey[challengeLookupKey(definition.timeWindow, definition.title)]
                    ?: return@forEach
            val completed = recordChallengeProgress(
                userId = userId,
                challengeId = challengeId,
                incrementBy = 1.0
            )
                .getOrDefault(false)
            if (completed) completedChallengeTitles += definition.title
        }

        return completedChallengeTitles
    }

    private suspend fun applyTripEndChallengeProgress(
        userId: String,
        tripDurationSec: Long
    ): List<String> {
        val completedChallengeTitles = mutableListOf<String>()
        val idByLookupKey = resolveHardcodedChallengeIds()
        val tripCountChallenges = hardcodedChallenges.filter {
            it.challengeType == ChallengeType.EXPLORE && it.key == "weekly_trip_1"
        }

        val timeChallenges = hardcodedChallenges.filter {
            it.challengeType == ChallengeType.TIME && it.key == "weekly_time_60"
        }

        tripCountChallenges.forEach { definition ->
            val challengeId =
                idByLookupKey[challengeLookupKey(definition.timeWindow, definition.title)]
                    ?: return@forEach
            val completed = recordChallengeProgress(
                userId = userId,
                challengeId = challengeId,
                incrementBy = 1.0
            )
                .getOrDefault(false)
            if (completed) completedChallengeTitles += definition.title
        }

        val minutes = (tripDurationSec / 60.0).coerceAtLeast(0.0)
        if (minutes <= 0.0) return completedChallengeTitles
        timeChallenges.forEach { definition ->
            val challengeId =
                idByLookupKey[challengeLookupKey(definition.timeWindow, definition.title)]
                    ?: return@forEach
            val completed = recordChallengeProgress(
                userId = userId,
                challengeId = challengeId,
                incrementBy = minutes
            )
                .getOrDefault(false)
            if (completed) completedChallengeTitles += definition.title
        }

        return completedChallengeTitles
    }

    suspend fun addXpForTrip(userId: String, xpAmount: Int): Result<Unit> = runCatching {
        if (xpAmount <= 0) return@runCatching
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching
        val newXpTotal = userRow.xpTotal + xpAmount.toLong()
        dataSource.updateUserXpTotal(userId = userRow.id, newXpTotal = newXpTotal)
        val newLevel = (newXpTotal / 1000).toInt() + 1
        if (newLevel != userRow.level.toInt()) {
            dataSource.updateUserLevel(userRow.id, newLevel)
        }
    }

    suspend fun getZonesForTrip(): Result<List<TripZone>> = runCatching {
        dataSource.fetchAllZones().mapNotNull { row ->
            val polygon = parseZoneBoundingBox(row.boundingBox) ?: return@mapNotNull null
            TripZone(id = row.id, name = row.name, polygon = polygon)
        }
    }

    private fun parseZoneBoundingBox(element: kotlinx.serialization.json.JsonElement): List<LatLng>? {
        return try {
            val array = element.jsonArray
            if (array.isEmpty()) return null
            // Try [{lat:.., lng:..}] format first, then [[lat, lng]] format
            array.map { item ->
                val obj = item.jsonObject
                val lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    ?: obj["latitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val lng = obj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    ?: obj["longitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
                if (lat != null && lng != null) LatLng(lat, lng)
                else {
                    // Fallback: [[lat, lng], ...]
                    val inner = item.jsonArray
                    val lat2 = inner[0].jsonPrimitive.content.toDouble()
                    val lng2 = inner[1].jsonPrimitive.content.toDouble()
                    LatLng(lat2, lng2)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateDailyExploreStreak(userId: String): Result<Int> = runCatching {
        val userRow = dataSource.getUserRowByAuthId(userId) ?: return@runCatching 0
        val today = LocalDate.now(ZoneOffset.UTC)
        val existing = dataSource.fetchStreakByType(userRow.id, "DAILY_EXPLORE")
        val lastDate = existing?.lastCountedDate?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

        // Already counted today — return current streak unchanged
        if (lastDate == today) return@runCatching existing.currentCount

        val newCount = if (lastDate == today.minusDays(1)) {
            existing.currentCount + 1  // consecutive day
        } else {
            1  // gap or first time
        }
        val bestCount = maxOf(newCount, existing?.bestCount ?: 0)

        dataSource.upsertStreak(
            StreakUpsert(
                userId = userRow.id,
                type = "DAILY_EXPLORE",
                currentCount = newCount,
                bestCount = bestCount,
                lastCountedDate = today.toString()
            )
        )
        newCount
    }

    suspend fun getFriendsApproxLocations(authUserId: String): Result<List<FriendMapMarker>> =
        runCatching {
            val currentUser = dataSource.getUserRowByAuthId(authUserId)
                ?: return@runCatching emptyList()
            val friendIds = dataSource.getUserFriends(currentUser.id)
            if (friendIds.isEmpty()) return@runCatching emptyList()

            val allUsers = dataSource.fetchLeaderboardUsers(1000)
            val friendUsers = allUsers.filter { it.id in friendIds }
            val allZones = dataSource.fetchAllZones()

            val markers = mutableListOf<FriendMapMarker>()
            for (friend in friendUsers) {
                val session = dataSource.fetchLatestZonedVisitSessionByUserId(friend.id) ?: continue
                val zoneId = session.zoneId ?: continue
                val zone = allZones.firstOrNull { it.id == zoneId } ?: continue
                val centroid = zoneCentroid(zone.boundingBox) ?: continue
                val initials = friend.displayName.trim().split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2).joinToString("")
                    .ifEmpty { friend.displayName.take(1).uppercase() }
                markers.add(FriendMapMarker(friend.displayName, initials, centroid))
            }
            markers
        }

    private fun zoneCentroid(element: kotlinx.serialization.json.JsonElement): GmsLatLng? {
        return try {
            val array = element.jsonArray
            if (array.isEmpty()) return null
            var sumLat = 0.0
            var sumLng = 0.0
            var count = 0
            for (item in array) {
                val obj = item.jsonObject
                val lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    ?: obj["latitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val lng = obj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    ?: obj["longitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
                if (lat != null && lng != null) {
                    sumLat += lat; sumLng += lng; count++
                }
            }
            if (count == 0) null else GmsLatLng(sumLat / count, sumLng / count)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getRouteStopLandmarks(routeId: Long): Result<List<Pair<Long, String>>> =
        runCatching {
            // Returns list of (landmarkId, placeId) for each stop that has a landmark
            val stops = dataSource.fetchRouteStopsByRouteId(routeId)
            val landmarkIds = stops.mapNotNull { it.landmarkId }
            if (landmarkIds.isEmpty()) return@runCatching emptyList()
            val landmarks = dataSource.fetchLandmarksByIds(landmarkIds)
            stops.sortedBy { it.orderIndex }.mapNotNull { stop ->
                val lm =
                    landmarks.firstOrNull { it.id == stop.landmarkId } ?: return@mapNotNull null
                Pair(lm.id, lm.placeId)
            }
        }

    // friendship management

    suspend fun sendFriendRequest(
        authUserId: String,
        recipientAuthUserId: String
    ): Result<Unit> = runCatching {
        val requesterUser = dataSource.getUserRowByAuthId(authUserId)
            ?: throw Exception("Current user not found")
        val recipientUser = dataSource.getUserRowByAuthId(recipientAuthUserId)
            ?: throw Exception("Recipient user not found")
        dataSource.sendFriendRequest(requesterUser.id, recipientUser.id)
    }

    suspend fun getIncomingFriendRequests(authUserId: String): Result<List<Pair<FriendRequestRow, User>>> =
        runCatching {
            val currentUser = dataSource.getUserRowByAuthId(authUserId)
                ?: throw Exception("Current user not found")

            val requests = dataSource.getIncomingFriendRequests(currentUser.id)
            val requesterIds = requests.map { it.requesterId }
            val requesters = if (requesterIds.isNotEmpty()) {
                dataSource.fetchLeaderboardUsers(1000)
                    .filter { it.id in requesterIds }
            } else {
                emptyList()
            }

            val requesterMap = requesters.associateBy { it.id }.mapValues { (_, userRow) ->
                User(
                    id = userRow.authUserId,
                    displayName = userRow.displayName,
                    username = userRow.username,
                    email = userRow.email,
                    avatarUrl = userRow.avatarUrl,
                    createdAtIso = userRow.createdAt,
                    homeCityId = null,
                    level = userRow.level.toInt(),
                    xpTotal = userRow.xpTotal.toInt()
                )
            }

            requests.mapNotNull { request ->
                val user = requesterMap[request.requesterId]
                if (user != null) request to user else null
            }
        }

    suspend fun getOutgoingFriendRequests(authUserId: String): Result<List<Pair<FriendRequestRow, User>>> =
        runCatching {
            val currentUser = dataSource.getUserRowByAuthId(authUserId)
                ?: throw Exception("Current user not found")

            val requests = dataSource.getOutgoingFriendRequests(currentUser.id)
            val recipientIds = requests.map { it.recipientId }
            val recipients = if (recipientIds.isNotEmpty()) {
                dataSource.fetchLeaderboardUsers(1000)
                    .filter { it.id in recipientIds }
            } else {
                emptyList()
            }

            val recipientMap = recipients.associateBy { it.id }.mapValues { (_, userRow) ->
                User(
                    id = userRow.authUserId,
                    displayName = userRow.displayName,
                    username = userRow.username,
                    email = userRow.email,
                    avatarUrl = userRow.avatarUrl,
                    createdAtIso = userRow.createdAt,
                    homeCityId = null,
                    level = userRow.level.toInt(),
                    xpTotal = userRow.xpTotal.toInt()
                )
            }

            requests.mapNotNull { request ->
                val user = recipientMap[request.recipientId]
                if (user != null) request to user else null
            }
        }

    suspend fun declineFriendRequest(requestId: Long): Result<Unit> = runCatching {
        dataSource.declineFriendRequest(requestId)
    }

    suspend fun cancelFriendRequest(requestId: Long): Result<Unit> = runCatching {
        dataSource.cancelFriendRequest(requestId)
    }

    suspend fun acceptFriendRequest(requestId: Long): Result<Unit> = runCatching {
        dataSource.acceptFriendRequest(requestId)
    }

    suspend fun getFriends(authUserId: String): Result<List<User>> = runCatching {
        val currentUser = dataSource.getUserRowByAuthId(authUserId)
            ?: throw Exception("Current user not found")

        val friendIds = dataSource.getUserFriends(currentUser.id)
        if (friendIds.isEmpty()) return@runCatching emptyList()

        val allUsers = dataSource.fetchLeaderboardUsers(1000)
        allUsers
            .filter { it.id in friendIds }
            .map { userRow ->
                User(
                    id = userRow.authUserId,
                    displayName = userRow.displayName,
                    username = userRow.username,
                    email = userRow.email,
                    avatarUrl = userRow.avatarUrl,
                    createdAtIso = userRow.createdAt,
                    homeCityId = null,
                    level = userRow.level.toInt(),
                    xpTotal = userRow.xpTotal.toInt()
                )
            }
    }

    suspend fun removeFriend(
        authUserId: String,
        friendAuthUserId: String
    ): Result<Unit> = runCatching {
        val currentUser = dataSource.getUserRowByAuthId(authUserId)
            ?: throw Exception("Current user not found")
        val friendUser = dataSource.getUserRowByAuthId(friendAuthUserId)
            ?: throw Exception("Friend user not found")
        dataSource.removeFriend(currentUser.id, friendUser.id)
    }

    suspend fun isFriend(authUserId: String, otherUserId: Long): Result<Boolean> = runCatching {
        val currentUser = dataSource.getUserRowByAuthId(authUserId)
            ?: throw Exception("Current user not found")
        dataSource.isFriend(currentUser.id, otherUserId)
    }

    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<User>> = runCatching {
        val userRows = dataSource.searchUsers(query, limit)
        userRows.map { userRow ->
            User(
                id = userRow.authUserId,
                displayName = userRow.displayName,
                username = userRow.username,
                email = userRow.email,
                avatarUrl = userRow.avatarUrl,
                createdAtIso = userRow.createdAt,
                homeCityId = null,
                level = userRow.level.toInt(),
                xpTotal = userRow.xpTotal.toInt()
            )
        }
    }

    suspend fun getFriendsLeaderboard(authUserId: String): Result<List<User>> = runCatching {
        val friends = getFriends(authUserId).getOrThrow()
        friends.sortedByDescending { it.xpTotal }
    }
}
