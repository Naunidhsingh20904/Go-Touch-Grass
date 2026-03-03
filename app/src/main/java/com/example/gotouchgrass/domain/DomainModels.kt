package com.example.gotouchgrass.domain

// Core value objects

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class GeoCell(
    val hash: String // e.g. geohash or H3 index
)

data class TimeRange(
    val startIso: String,
    val endIso: String?
)

@JvmInline
value class RarityScore(val value: Double)

// 1) Identity, accounts, and sessions

data class User(
    val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val createdAtIso: String,
    val homeCityId: String?,
    val level: Int,
    val xpTotal: Int
)

enum class MapVisibilityMode {
    FRIENDS_ONLY,
    CITY_LEVEL,
    GLOBAL
}

enum class UnitsPreference {
    METRIC,
    IMPERIAL
}

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val userId: String,
    val locationTrackingEnabled: Boolean,
    val shareApproxLocation: Boolean,
    val mapVisibilityMode: MapVisibilityMode,
    val pushEnabled: Boolean,
    val units: UnitsPreference,
    val theme: ThemePreference
)

// 2) Map world: cities, zones, landmarks

data class City(
    val id: String,
    val name: String,
    val country: String,
    val timezone: String,
    val boundingBox: List<LatLng> // usually 4 points (polygon)
)

enum class ZoneType {
    CAMPUS,
    BUILDING,
    NEIGHBORHOOD,
    PARK
}

data class Zone(
    val id: String,
    val cityId: String,
    val name: String,
    val type: ZoneType,
    val boundary: List<LatLng>,
    val centerLatLng: LatLng
)

enum class LandmarkCategory {
    MURAL,
    STATUE,
    LOUNGE,
    CAFE,
    PARK,
    STUDY_SPOT,
    OTHER
}

data class Landmark(
    val id: String,
    val zoneId: String,
    val name: String,
    val category: LandmarkCategory,
    val latLng: LatLng,
    val radiusMeters: Double,
    val description: String?,
    val photoRef: String?,
    val createdByUserId: String?,
    val isVerified: Boolean
)

// 3) Location + time spent (core tracking)

enum class LocationSource {
    GPS,
    WIFI
}

data class LocationPing(
    val id: String,
    val userId: String,
    val timestampIso: String,
    val approxLatLng: LatLng,
    val accuracyMeters: Double,
    val speedMetersPerSecond: Double?,
    val source: LocationSource,
    val hashCell: GeoCell
)

enum class VisitSessionSource {
    AUTO,
    MANUAL_ADJUSTED
}

data class VisitSession(
    val id: String,
    val userId: String,
    val zoneId: String,
    val startedAtIso: String,
    val endedAtIso: String?,
    val durationSec: Long,
    val confidenceScore: Double,
    val source: VisitSessionSource,
    val isStudySession: Boolean
)

enum class ZoneEntryEventType {
    ENTER,
    EXIT
}

data class ZoneEntryEvent(
    val id: String,
    val userId: String,
    val zoneId: String,
    val type: ZoneEntryEventType,
    val timestampIso: String
)

// 4) Capturing, ownership, XP, rewards (gamification loop)

enum class CaptureProofType {
    GPS,
    GEOFENCE,
    QR
}

data class Capture(
    val id: String,
    val userId: String,
    val zoneId: String,
    val landmarkId: String?,
    val capturedAtIso: String,
    val proofType: CaptureProofType,
    val rarityAtTime: RarityScore,
    val xpAwarded: Int,
    val coinsAwarded: Int
)

data class ZoneOwnership(
    val zoneId: String,
    val ownerUserId: String,
    val ownedSinceIso: String,
    val ownershipScore: Double,
    val lastContestedAtIso: String?
)

enum class OwnershipChangeReason {
    CAPTURED,
    CONTESTED,
    DECAYED
}

data class ZoneOwnershipHistory(
    val id: String,
    val zoneId: String,
    val previousOwnerId: String?,
    val newOwnerId: String,
    val changedAtIso: String,
    val reason: OwnershipChangeReason
)

enum class XPEventType {
    VISIT_TIME,
    CAPTURE,
    BONUS,
    CHALLENGE
}

data class XPEvent(
    val id: String,
    val userId: String,
    val type: XPEventType,
    val amount: Int,
    val createdAtIso: String,
    val refId: String?
)

// 5) Badges, milestones, streaks, completion

enum class BadgeRuleType {
    SIMPLE_THRESHOLD,
    STREAK,
    EXPLORATION,
    SOCIAL,
    CUSTOM
}

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val ruleType: BadgeRuleType,
    val ruleConfigJson: String
)

data class UserBadge(
    val userId: String,
    val badgeId: String,
    val earnedAtIso: String
)

data class MilestoneProgress(
    val userId: String,
    val milestoneId: String,
    val progressValue: Double,
    val lastUpdatedAtIso: String
)

enum class StreakType {
    DAILY_EXPLORE,
    WEEKLY_CAPTURE
}

data class Streak(
    val userId: String,
    val type: StreakType,
    val currentCount: Int,
    val bestCount: Int,
    val lastCountedDateIso: String
)

data class CityCompletion(
    val userId: String,
    val cityId: String,
    val zonesVisitedCount: Int,
    val zonesTotal: Int,
    val completionPct: Double
)

// 6) Challenges and expeditions (light guidance)

enum class ChallengeTimeWindow {
    DAILY,
    WEEKLY
}

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val timeWindow: ChallengeTimeWindow,
    val ruleConfigJson: String,
    val rewardXP: Int,
    val rewardCoins: Int
)

data class ChallengeProgress(
    val userId: String,
    val challengeId: String,
    val periodStartIso: String,
    val progressValue: Double,
    val completedAtIso: String?
)

enum class RouteTheme {
    MURALS,
    PARKS,
    HIDDEN_STUDY_SPOTS,
    FOOD,
    DRINKS,
    CITY_HIGHLIGHTS
}

enum class RouteDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class Route(
    val id: String,
    val cityId: String,
    val title: String,
    val theme: RouteTheme,
    val difficulty: RouteDifficulty,
    val estimatedDurationMinutes: Int,
    val createdByUserId: String?,
    val isOfficial: Boolean
)

data class RouteStop(
    val id: String,
    val routeId: String,
    val orderIndex: Int,
    val zoneId: String?,
    val landmarkId: String?,
    val hintText: String?
)

data class UserRouteProgress(
    val userId: String,
    val routeId: String,
    val startedAtIso: String,
    val completedAtIso: String?,
    val stopsCompleted: Int
)

// 7) Social layer: friends, competition, leaderboards

enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}

data class Friendship(
    val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: FriendshipStatus,
    val createdAtIso: String
)

enum class LeaderboardScope {
    CAMPUS,
    CITY,
    GLOBAL,
    FRIENDS
}

enum class LeaderboardMetric {
    XP_WEEK,
    ZONES_CAPTURED
}

enum class LeaderboardPeriod {
    WEEKLY,
    MONTHLY
}

data class Leaderboard(
    val id: String,
    val scope: LeaderboardScope,
    val metric: LeaderboardMetric,
    val period: LeaderboardPeriod
)

data class LeaderboardEntry(
    val leaderboardId: String,
    val userId: String,
    val rank: Int,
    val value: Double,
    val computedAtIso: String
)

enum class ZoneActivityType {
    CAPTURE,
    VISIT,
    OWNERSHIP_CHANGE
}

data class ZoneActivity(
    val id: String,
    val zoneId: String,
    val type: ZoneActivityType,
    val userId: String,
    val createdAtIso: String,
    val summaryText: String
)

// 8) Media integrations (transit playlists / contextual media)

enum class PlaylistProvider {
    SPOTIFY,
    APPLE
}

data class ZonePlaylist(
    val id: String,
    val zoneId: String,
    val provider: PlaylistProvider,
    val playlistUrl: String,
    val title: String,
    val createdByUserId: String?
)

// 9) Summaries + reflection

data class ExplorationSummary(
    val id: String,
    val userId: String,
    val periodStartIso: String,
    val periodEndIso: String,
    val topZonesJson: String,
    val newZonesVisited: Int,
    val timeByZoneJson: String,
    val insightsText: String?
)

// 10) Privacy, safety

data class ApproxPresence(
    val userId: String,
    val zoneId: String?,
    val hashCell: GeoCell,
    val lastUpdatedAtIso: String
)

