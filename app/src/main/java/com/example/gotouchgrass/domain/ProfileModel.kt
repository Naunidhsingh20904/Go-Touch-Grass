package com.example.gotouchgrass.domain

import com.example.gotouchgrass.data.ProfileRepository

/**
 * Domain‑level model for the Profile screen.
 *
 * This sits between the ViewModel and the data layer and is the thing
 * we write unit tests against, using a mock implementation of [ProfileRepository].
 */
class ProfileModel(
    private val currentUserId: String,
    private val repository: ProfileRepository
) {

    suspend fun getUser(): User? = repository.getUser(currentUserId).getOrNull()

    suspend fun updateProfile(
        username: String,
        displayName: String,
        avatarKey: String?
    ): Result<Unit> =
        repository.updateProfile(currentUserId, username, displayName, avatarKey)

    suspend fun getLifetimeStats(): LifetimeStats? =
        repository.getLifetimeStats(currentUserId).getOrNull()

    suspend fun getStreakData(): StreakData? =
        repository.getStreakData(currentUserId).getOrNull()

    suspend fun getWeeklySummary(): WeeklySummary? =
        repository.getWeeklySummary(currentUserId).getOrNull()

    suspend fun getFriends(): List<User> =
        repository.getFriends(currentUserId).getOrNull() ?: emptyList()
}

