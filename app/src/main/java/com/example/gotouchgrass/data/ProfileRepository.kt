package com.example.gotouchgrass.data

import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.LifetimeStats
import com.example.gotouchgrass.domain.StreakData
import com.example.gotouchgrass.domain.StreakType
import com.example.gotouchgrass.domain.User
import com.example.gotouchgrass.domain.WeeklySummary

/**
 * Profile‑focused data access interface. This is the abstraction that
 * the domain/model layer depends on so we can swap the real DB with a mock.
 */
interface ProfileRepository {
    suspend fun getUser(userId: String): Result<User?>
    suspend fun updateProfile(
        userId: String,
        username: String,
        displayName: String,
        avatarKey: String?
    ): Result<Unit>
    suspend fun getLifetimeStats(userId: String): Result<LifetimeStats>
    suspend fun getStreakData(userId: String): Result<StreakData>
    suspend fun getWeeklySummary(userId: String): Result<WeeklySummary>
}

/**
 * Real implementation backed by [GoTouchGrassRepository] / Supabase.
 */
class SupabaseProfileRepository(
    private val repository: GoTouchGrassRepository
) : ProfileRepository {

    override suspend fun getUser(userId: String): Result<User?> = repository.getUser(userId)

    override suspend fun updateProfile(
        userId: String,
        username: String,
        displayName: String,
        avatarKey: String?
    ): Result<Unit> = repository.updateUserProfile(userId, username, displayName, avatarKey)

    override suspend fun getLifetimeStats(userId: String): Result<LifetimeStats> =
        repository.getLifetimeStats(userId)

    override suspend fun getStreakData(userId: String): Result<StreakData> =
        repository.getStreakData(userId)

    override suspend fun getWeeklySummary(userId: String): Result<WeeklySummary> =
        repository.getWeeklySummary(userId)
}

/**
 * Mock implementation that mimics profile‑related DB behaviour using [FakeData].
 * Used in unit tests so we never depend on live Supabase data.
 */
class FakeProfileRepository : ProfileRepository {

    override suspend fun getUser(userId: String): Result<User?> = runCatching {
        FakeData.users.find { it.id == userId }
    }

    override suspend fun updateProfile(
        userId: String,
        username: String,
        displayName: String,
        avatarKey: String?
    ): Result<Unit> = runCatching {
        // testing
    }

    override suspend fun getLifetimeStats(userId: String): Result<LifetimeStats> = runCatching {
        // Derive lifetime stats from FakeData. For now we keep it simple and
        // focus on total XP; the other fields are placeholders that could be
        // refined later as needed.
        val user = FakeData.users.find { it.id == userId }
        LifetimeStats(
            totalXp = user?.xpTotal ?: 0,
            totalDistanceKm = 0f,
            citiesExplored = 0
        )
    }

    override suspend fun getStreakData(userId: String): Result<StreakData> = runCatching {
        val streak = FakeData.streaks
            .find { it.userId == userId && it.type == StreakType.DAILY_EXPLORE }
        if (streak == null) {
            StreakData(currentDays = 0, bestDays = 0)
        } else {
            StreakData(currentDays = streak.currentCount, bestDays = streak.bestCount)
        }
    }

    override suspend fun getWeeklySummary(userId: String): Result<WeeklySummary> = runCatching {
        // The exact numbers are less important than being deterministic for tests.
        // These values are chosen to align with expectations in Profile tests.
        WeeklySummary(
            timeOutside = "4h",
            zonesVisited = 5,
            xpEarned = 0,
            dailyActivity = listOf(0.2f, 0.5f, 0.8f, 0.3f, 0.0f, 0.0f, 0.0f)
        )
    }
}

