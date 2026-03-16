package com.example.gotouchgrass.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// supabase models should mirror DB columns and types, not domain model shape

@Serializable
data class UserRow(
    val id: Long,
    @SerialName("auth_user_id") val authUserId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("display_name") val displayName: String,
    val username: String,
    val email: String,
    @SerialName("avatar_url") val avatarUrl: Int? = null,
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
    @SerialName("zone_id") val zoneId: Long,
    val category: String,
    @SerialName("place_id") val placeId: Long,
    @SerialName("created_by_user_id") val createdByUserId: Long? = null,
    @SerialName("is_verified") val isVerified: Boolean
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
    @SerialName("progress_value") val progressValue: Double,
    @SerialName("completed_at") val completedAt: String? = null
)

@Serializable
data class ChallengeXpAwardInsert(
    @SerialName("user_id") val userId: Long,
    @SerialName("challenge_id") val challengeId: Long,
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
