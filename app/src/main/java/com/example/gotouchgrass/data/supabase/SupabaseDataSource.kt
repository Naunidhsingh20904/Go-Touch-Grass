package com.example.gotouchgrass.data.supabase

import com.example.gotouchgrass.domain.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

open class SupabaseDataSource(
    private val supabaseClient: SupabaseClient
) {

    companion object {
        const val TABLE_USERS = "user"
        const val TABLE_CITIES = "city"
        const val TABLE_ZONES = "zone"
        const val TABLE_LANDMARKS = "landmark"
        const val TABLE_CHALLENGES = "challenge"
        const val TABLE_CHALLENGE_PROGRESS = "challenge_progress"
        const val TABLE_CHALLENGE_XP_AWARD = "challenge_xp_award"
        const val TABLE_ROUTES = "route"
        const val TABLE_ROUTE_STOPS = "route_stop"
        const val TABLE_SEARCH_ACTIVITY = "search_activity"
        const val TABLE_USER_SETTINGS = "user_settings"
        const val TABLE_STREAKS = "streak"
        const val TABLE_VISIT_SESSIONS = "visit_session"
        const val TABLE_CAPTURES = "capture"
        const val TABLE_FRIEND_REQUESTS = "friend_request"
        const val TABLE_FRIENDSHIPS = "friendship"
    }

    suspend fun getUserById(userId: String): Result<User?> = runCatching {
        val users = supabaseClient.from(TABLE_USERS).select().decodeList<UserRow>()

        users.firstOrNull { it.authUserId == userId }?.toDomainUser()
    }

    private fun UserRow.toDomainUser(): User {
        return User(
            id = authUserId,
            displayName = displayName,
            username = username,
            email = email,
            avatarUrl = avatarUrl,
            createdAtIso = createdAt,
            homeCityId = null,
            level = level.toInt(),
            xpTotal = xpTotal.toInt()
        )
    }

    suspend fun updateUserProfileByAuthId(
        authUserId: String,
        username: String,
        displayName: String,
        avatarKey: String?
    ) {
        supabaseClient.from(TABLE_USERS).update(
            update = {
                this["username"] = username
                this["display_name"] = displayName
                if (avatarKey == null) {
                    setToNull("avatar_url")
                } else {
                    this["avatar_url"] = avatarKey
                }
            }
        ) {
            filter {
                eq("auth_user_id", authUserId)
            }
        }
    }

    suspend fun getUserRowByAuthId(authUserId: String): UserRow? =
        supabaseClient.from(TABLE_USERS).select().decodeList<UserRow>()
            .firstOrNull { it.authUserId == authUserId }

    suspend fun fetchChallenges(timeWindow: String): List<ChallengeRow> =
        supabaseClient.from(TABLE_CHALLENGES).select().decodeList<ChallengeRow>()
            .filter { it.timeWindow == timeWindow }

    suspend fun fetchChallengeProgress(userId: Long): List<ChallengeProgressRow> =
        supabaseClient.from(TABLE_CHALLENGE_PROGRESS).select().decodeList<ChallengeProgressRow>()
            .filter { it.userId == userId }

    suspend fun upsertChallengeProgress(row: ChallengeProgressRow) {
        supabaseClient.from(TABLE_CHALLENGE_PROGRESS)
            .upsert(row) { onConflict = "user_id,challenge_id" }
    }

    suspend fun tryInsertChallengeXpAward(row: ChallengeXpAwardInsert): Boolean {
        val result = runCatching {
            supabaseClient.from(TABLE_CHALLENGE_XP_AWARD).insert(row)
        }
        return result.isSuccess
    }

    suspend fun updateUserXpTotal(userId: Long, newXpTotal: Long) {
        supabaseClient.from(TABLE_USERS).update(UserXpUpdate(newXpTotal)) {
            filter {
                eq("id", userId)
            }
        }
    }

    suspend fun insertSearchActivity(row: SearchActivityInsert) {
        supabaseClient.from(TABLE_SEARCH_ACTIVITY).insert(row)
    }

    suspend fun fetchRecentSearchActivity(userId: Long, limit: Int): List<SearchActivityRow> {
        val safeLimit = limit.coerceAtLeast(1)
        return supabaseClient.from(TABLE_SEARCH_ACTIVITY).select {
            filter {
                eq("user_id", userId)
            }
            order("created_at", Order.DESCENDING)
            limit(safeLimit.toLong())
        }.decodeList()
    }

    suspend fun fetchRecentGlobalSearchActivity(limit: Int): List<SearchActivityRow> {
        val safeLimit = limit.coerceAtLeast(1)
        return supabaseClient.from(TABLE_SEARCH_ACTIVITY).select {
            order("created_at", Order.DESCENDING)
            limit(safeLimit.toLong())
        }.decodeList()
    }

    suspend fun fetchRoutes(): List<RouteRow> =
        supabaseClient.from(TABLE_ROUTES).select().decodeList<RouteRow>()

    suspend fun fetchLandmarkByPlaceId(placeId: String): LandmarkRow? =
        supabaseClient.from(TABLE_LANDMARKS).select {
            filter { eq("place_id", placeId.trim()) }
            limit(1)
        }.decodeList<LandmarkRow>().firstOrNull()

    suspend fun fetchLandmarksByIds(ids: List<Long>): List<LandmarkRow> {
        if (ids.isEmpty()) return emptyList()
        return supabaseClient.from(TABLE_LANDMARKS).select {
            filter { isIn("id", ids) }
        }.decodeList<LandmarkRow>()
    }

    suspend fun hasCaptureForUserAndLandmark(userId: Long, landmarkId: Long): Boolean =
        supabaseClient.from(TABLE_CAPTURES).select {
            filter {
                eq("user_id", userId)
                eq("landmark_id", landmarkId)
            }
            limit(1)
        }.decodeList<CaptureRow>().isNotEmpty()

    suspend fun fetchLatestCaptureByUserAndLandmark(userId: Long, landmarkId: Long): CaptureRow? =
        supabaseClient.from(TABLE_CAPTURES).select {
            filter {
                eq("user_id", userId)
                eq("landmark_id", landmarkId)
            }
            order("created_at", Order.DESCENDING)
            limit(1)
        }.decodeList<CaptureRow>().firstOrNull()

    suspend fun fetchCapturedLandmarkIdsByUser(userId: Long): List<Long> =
        supabaseClient.from(TABLE_CAPTURES).select {
            filter { eq("user_id", userId) }
        }.decodeList<CaptureRow>().mapNotNull { it.landmarkId }.distinct()

    suspend fun insertCapture(row: CaptureInsert) {
        supabaseClient.from(TABLE_CAPTURES).insert(row)
    }

    suspend fun fetchAllRouteStops(): List<RouteStopRow> =
        supabaseClient.from(TABLE_ROUTE_STOPS).select().decodeList<RouteStopRow>()

    suspend fun fetchUserSettings(userId: Long): UserSettingsRow? =
        supabaseClient.from(TABLE_USER_SETTINGS).select {
            filter { eq("user_id", userId) }
            limit(1)
        }.decodeList<UserSettingsRow>().firstOrNull()

    suspend fun upsertUserSettings(row: UserSettingsUpsert) {
        supabaseClient.from(TABLE_USER_SETTINGS).upsert(row) { onConflict = "user_id" }
    }

    suspend fun fetchLeaderboardUsers(limit: Int = 20): List<UserRow> =
        supabaseClient.from(TABLE_USERS).select {
            order("xp_total", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList()

    suspend fun fetchStreakByType(userId: Long, type: String): StreakRow? =
        supabaseClient.from(TABLE_STREAKS).select {
            filter {
                eq("user_id", userId)
                eq("type", type)
            }
            limit(1)
        }.decodeList<StreakRow>().firstOrNull()

    suspend fun fetchWeeklyVisitSessions(
        userId: Long,
        weekStartIso: String
    ): List<VisitSessionRow> = supabaseClient.from(TABLE_VISIT_SESSIONS).select {
        filter {
            eq("user_id", userId)
            gte("started_at", weekStartIso)
        }
    }.decodeList()

    // friendship management

    suspend fun sendFriendRequest(requesterId: Long, recipientId: Long) {
        supabaseClient.from(TABLE_FRIEND_REQUESTS).insert(
            FriendRequestInsert(
                requesterId = requesterId,
                recipientId = recipientId
            )
        )
    }

    suspend fun getIncomingFriendRequests(userId: Long): List<FriendRequestRow> =
        supabaseClient.from(TABLE_FRIEND_REQUESTS).select {
            filter {
                eq("recipient_id", userId)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    suspend fun getOutgoingFriendRequests(userId: Long): List<FriendRequestRow> =
        supabaseClient.from(TABLE_FRIEND_REQUESTS).select {
            filter {
                eq("requester_id", userId)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    suspend fun declineFriendRequest(requestId: Long) {
        supabaseClient.from(TABLE_FRIEND_REQUESTS).delete {
            filter {
                eq("id", requestId)
            }
        }
    }

    suspend fun cancelFriendRequest(requestId: Long) {
        supabaseClient.from(TABLE_FRIEND_REQUESTS).delete {
            filter {
                eq("id", requestId)
            }
        }
    }

    suspend fun acceptFriendRequest(requestId: Long) {
        supabaseClient.postgrest.rpc(
            function = "accept_friend_request",
            parameters = mapOf("p_request_id" to requestId)
        )
    }

    suspend fun getUserFriends(userId: Long): List<Long> {
        val friendships = supabaseClient.from(TABLE_FRIENDSHIPS).select {
            filter {
                eq("user_id_a", userId)
            }
        }.decodeList<FriendshipRow>()

        val asFriends = friendships.map { it.userIdB }

        val reverseFriendships = supabaseClient.from(TABLE_FRIENDSHIPS).select {
            filter {
                eq("user_id_b", userId)
            }
        }.decodeList<FriendshipRow>()

        val bFriends = reverseFriendships.map { it.userIdA }

        return (asFriends + bFriends).distinct()
    }

    suspend fun isFriend(userId: Long, friendId: Long): Boolean {
        val userIdA = minOf(userId, friendId)
        val userIdB = maxOf(userId, friendId)

        return supabaseClient.from(TABLE_FRIENDSHIPS).select {
            filter {
                eq("user_id_a", userIdA)
                eq("user_id_b", userIdB)
            }
            limit(1)
        }.decodeList<FriendshipRow>().isNotEmpty()
    }

    suspend fun removeFriend(userId: Long, friendId: Long) {
        val userIdA = minOf(userId, friendId)
        val userIdB = maxOf(userId, friendId)

        supabaseClient.from(TABLE_FRIENDSHIPS).delete {
            filter {
                eq("user_id_a", userIdA)
                eq("user_id_b", userIdB)
            }
        }
    }

    suspend fun searchUsers(query: String, limit: Int = 20): List<UserRow> {
        val safeLimit = limit.coerceAtLeast(1)
        val trimmedQuery = query.trim()

        val rpcResult = runCatching {
            supabaseClient.postgrest.rpc(
                function = "search_users_for_friends",
                parameters = mapOf(
                    "p_query" to trimmedQuery,
                    "p_limit" to safeLimit
                )
            ).decodeList<UserRow>()
        }

        if (rpcResult.isSuccess) {
            return rpcResult.getOrThrow()
        }

        return supabaseClient.from(TABLE_USERS).select {
            filter {
                or {
                    ilike("username", "%$trimmedQuery%")
                    ilike("display_name", "%$trimmedQuery%")
                }
            }
            limit(safeLimit.toLong())
        }.decodeList()
    }
}