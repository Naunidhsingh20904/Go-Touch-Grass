package com.example.gotouchgrass.data.supabase

import com.example.gotouchgrass.domain.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseDataSource(
    private val supabaseClient: SupabaseClient
) {

    companion object {
        const val TABLE_USERS = "user"
        const val TABLE_CITIES = "city"
        const val TABLE_ZONES = "zone"
        const val TABLE_LANDMARKS = "landmark"
        const val TABLE_CHALLENGES = "challenge"
        const val TABLE_CHALLENGE_PROGRESS = "challenge_progress"
        const val TABLE_ROUTES = "route"
        const val TABLE_ROUTE_STOPS = "route_stop"
    }

    // user operations

    suspend fun getUserById(userId: String): Result<User?> = runCatching {
        val users = supabaseClient.from(TABLE_USERS)
            .select()
            .decodeList<UserRow>()

        users.firstOrNull { it.authUserId == userId }?.toDomainUser()
    }

    private fun UserRow.toDomainUser(): User {
        return User(
            id = authUserId,
            displayName = displayName,
            username = username,
            email = email,
            avatarUrl = avatarUrl?.toString(),
            createdAtIso = createdAt,
            homeCityId = null,
            level = level.toInt(),
            xpTotal = xpTotal.toInt()
        )
    }

    // Explore page

    suspend fun getUserRowByAuthId(authUserId: String): UserRow? =
        supabaseClient.from(TABLE_USERS).select().decodeList<UserRow>()
            .firstOrNull { it.authUserId == authUserId }

    suspend fun fetchChallenges(timeWindow: String): List<ChallengeRow> =
        supabaseClient.from(TABLE_CHALLENGES).select().decodeList<ChallengeRow>()
            .filter { it.timeWindow == timeWindow }

    suspend fun fetchChallengeProgress(userId: Long): List<ChallengeProgressRow> =
        supabaseClient.from(TABLE_CHALLENGE_PROGRESS).select().decodeList<ChallengeProgressRow>()
            .filter { it.userId == userId }

    suspend fun fetchRoutes(): List<RouteRow> =
        supabaseClient.from(TABLE_ROUTES).select().decodeList<RouteRow>()

    suspend fun fetchAllRouteStops(): List<RouteStopRow> =
        supabaseClient.from(TABLE_ROUTE_STOPS).select().decodeList<RouteStopRow>()
}
