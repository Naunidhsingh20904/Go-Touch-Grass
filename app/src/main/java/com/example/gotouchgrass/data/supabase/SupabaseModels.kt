package com.example.gotouchgrass.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserRow(
    val id: Long,
    @SerialName("auth_user_id") val authUserId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("display_name") val displayName: String,
    val username: String,
    val email: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val level: Long,
    @SerialName("xp_total") val xpTotal: Long
)

@Serializable
data class CityRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    val name: String,
    val country: String,
    @SerialName("bounding_box") val boundingBox: JsonElement
)

@Serializable
data class ZoneRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("city_id") val cityId: Long,
    val name: String,
    val type: String,
    @SerialName("bounding_box") val boundingBox: JsonElement
)

@Serializable
data class LandmarkRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("zone_id") val zoneId: Long? = null,
    val category: String? = null,
    @SerialName("place_id") val placeId: String,
    @SerialName("created_by_user_id") val createdByUserId: Long? = null,
    @SerialName("is_verified") val isVerified: Boolean? = null
)

@Serializable
data class LandmarkInsert(
    @SerialName("zone_id") val zoneId: Long? = null,
    val category: String? = null,
    @SerialName("place_id") val placeId: String,
    @SerialName("created_by_user_id") val createdByUserId: Long? = null,
    @SerialName("is_verified") val isVerified: Boolean? = null
)

@Serializable
data class ChallengeRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    val title: String,
    val description: String,
    @SerialName("challenge_type") val challengeType: String,
    @SerialName("time_window") val timeWindow: String,
    @SerialName("rule_config_json") val ruleConfigJson: JsonElement,
    @SerialName("reward_xp") val rewardXp: Long
)

@Serializable
data class ChallengeProgressRow(
    @SerialName("user_id") val userId: Long,
    @SerialName("challenge_id") val challengeId: Long,
    @SerialName("period_start_iso") val periodStartIso: String? = null,
    @SerialName("progress_value") val progressValue: Double,
    @SerialName("completed_at") val completedAt: String? = null
)

@Serializable
data class ChallengeXpAwardInsert(
    @SerialName("user_id") val userId: Long,
    @SerialName("challenge_id") val challengeId: Long,
    @SerialName("period_start_iso") val periodStartIso: String? = null,
    @SerialName("awarded_xp") val awardedXp: Long
)

@Serializable
data class UserXpUpdate(
    @SerialName("xp_total") val xpTotal: Long
)

@Serializable
data class SearchActivityRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("query_text") val queryText: String,
    @SerialName("selected_place_id") val selectedPlaceId: String? = null,
    @SerialName("selected_title") val selectedTitle: String? = null,
    val source: String
)

@Serializable
data class SearchActivityInsert(
    @SerialName("user_id") val userId: Long,
    @SerialName("query_text") val queryText: String,
    @SerialName("selected_place_id") val selectedPlaceId: String? = null,
    @SerialName("selected_title") val selectedTitle: String? = null,
    val source: String
)

@Serializable
data class RouteRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("city_id") val cityId: Long,
    val title: String,
    val theme: String,
    val difficulty: String,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int,
    @SerialName("created_by_user_id") val createdByUserId: Long? = null,
    @SerialName("is_verified") val isVerified: Boolean
)

@Serializable
data class RouteStopRow(
    val id: Long,
    @SerialName("route_id") val routeId: Long,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("landmark_id") val landmarkId: Long? = null,
    @SerialName("hint_text") val hintText: String? = null
)

@Serializable
data class UserSettingsRow(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("sound_effects_enabled") val soundEffectsEnabled: Boolean = true,
    @SerialName("dark_mode_enabled") val darkModeEnabled: Boolean = false,
    @SerialName("location_services_enabled") val locationServicesEnabled: Boolean = true
)

@Serializable
data class UserSettingsUpsert(
    @SerialName("user_id") val userId: Long,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean,
    @SerialName("sound_effects_enabled") val soundEffectsEnabled: Boolean,
    @SerialName("dark_mode_enabled") val darkModeEnabled: Boolean,
    @SerialName("location_services_enabled") val locationServicesEnabled: Boolean
)

@Serializable
data class StreakRow(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    val type: String,
    @SerialName("current_count") val currentCount: Int,
    @SerialName("best_count") val bestCount: Int,
    @SerialName("last_counted_date") val lastCountedDate: String? = null
)

@Serializable
data class VisitSessionRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("zone_id") val zoneId: Long? = null,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_sec") val durationSec: Long,
    @SerialName("confidence_score") val confidenceScore: Double = 1.0,
    val source: String,
    @SerialName("is_study_session") val isStudySession: Boolean = false
)

@Serializable
data class StreakUpsert(
    @SerialName("user_id") val userId: Long,
    val type: String,
    @SerialName("current_count") val currentCount: Int,
    @SerialName("best_count") val bestCount: Int,
    @SerialName("last_counted_date") val lastCountedDate: String
)

@Serializable
data class VisitSessionInsert(
    @SerialName("user_id") val userId: Long,
    @SerialName("zone_id") val zoneId: Long? = null,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String,
    @SerialName("duration_sec") val durationSec: Long,
    @SerialName("confidence_score") val confidenceScore: Double = 1.0,
    val source: String = "AUTO",
    @SerialName("is_study_session") val isStudySession: Boolean = false
)

@Serializable
data class CaptureRow(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("zone_id") val zoneId: Long? = null,
    @SerialName("landmark_id") val landmarkId: Long? = null,
    @SerialName("captured_at") val capturedAt: String? = null,
    @SerialName("rarity_at_time") val rarityAtTime: Double,
    @SerialName("xp_awarded") val xpAwarded: Int
)

@Serializable
data class CaptureInsert(
    @SerialName("user_id") val userId: Long,
    @SerialName("zone_id") val zoneId: Long? = null,
    @SerialName("landmark_id") val landmarkId: Long? = null,
    @SerialName("rarity_at_time") val rarityAtTime: Double,
    @SerialName("xp_awarded") val xpAwarded: Int
)

@Serializable
data class FriendRequestRow(
    val id: Long,
    @SerialName("requester_id") val requesterId: Long,
    @SerialName("recipient_id") val recipientId: Long,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class FriendRequestInsert(
    @SerialName("requester_id") val requesterId: Long,
    @SerialName("recipient_id") val recipientId: Long
)

@Serializable
data class FriendshipRow(
    val id: Long,
    @SerialName("user_id_a") val userIdA: Long,
    @SerialName("user_id_b") val userIdB: Long,
    @SerialName("created_at") val createdAt: String
)
