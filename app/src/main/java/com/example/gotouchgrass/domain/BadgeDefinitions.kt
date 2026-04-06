package com.example.gotouchgrass.domain

enum class BadgeThresholdType { CAPTURES, FRIENDS, LEVEL, STREAK_DAYS, ZONES_VISITED }

data class BadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val threshold: Int,
    val ruleType: BadgeThresholdType
)

data class BadgeStatus(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val isUnlocked: Boolean
)

val ALL_BADGES: List<BadgeDefinition> = listOf(
    BadgeDefinition("first_step",         "First Step",          "Capture your first landmark",       "location_on",  1,  BadgeThresholdType.CAPTURES),
    BadgeDefinition("trailblazer",        "Trailblazer",         "Capture 3 landmarks",               "star",         3,  BadgeThresholdType.CAPTURES),
    BadgeDefinition("explorer",           "Explorer",            "Capture 5 landmarks",               "explore",      5,  BadgeThresholdType.CAPTURES),
    BadgeDefinition("dedicated_explorer", "Dedicated Explorer",  "Capture 10 landmarks",              "thumb_up",     10, BadgeThresholdType.CAPTURES),
    BadgeDefinition("social_butterfly",   "Social Butterfly",    "Add your first friend",             "favorite",     1,  BadgeThresholdType.FRIENDS),
    BadgeDefinition("rising_star",        "Rising Star",         "Reach level 2",                     "trending_up",  2,  BadgeThresholdType.LEVEL),
    BadgeDefinition("streak_starter",     "Streak Starter",      "Maintain a 3-day streak",           "date_range",   3,  BadgeThresholdType.STREAK_DAYS),
    BadgeDefinition("zone_scout",         "Zone Scout",          "Visit at least one zone this week", "place",        1,  BadgeThresholdType.ZONES_VISITED),
)
