package com.example.gotouchgrass.domain

enum class BadgeRuleType { CAPTURES, FRIENDS, LEVEL, STREAK_DAYS, ZONES_VISITED }

data class BadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val threshold: Int,
    val ruleType: BadgeRuleType
)

data class BadgeStatus(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val isUnlocked: Boolean
)

val ALL_BADGES: List<BadgeDefinition> = listOf(
    BadgeDefinition("first_step",         "First Step",          "Capture your first landmark",       "location_on",  1,  BadgeRuleType.CAPTURES),
    BadgeDefinition("trailblazer",        "Trailblazer",         "Capture 3 landmarks",               "star",         3,  BadgeRuleType.CAPTURES),
    BadgeDefinition("explorer",           "Explorer",            "Capture 5 landmarks",               "explore",      5,  BadgeRuleType.CAPTURES),
    BadgeDefinition("dedicated_explorer", "Dedicated Explorer",  "Capture 10 landmarks",              "thumb_up",     10, BadgeRuleType.CAPTURES),
    BadgeDefinition("social_butterfly",   "Social Butterfly",    "Add your first friend",             "favorite",     1,  BadgeRuleType.FRIENDS),
    BadgeDefinition("rising_star",        "Rising Star",         "Reach level 2",                     "trending_up",  2,  BadgeRuleType.LEVEL),
    BadgeDefinition("streak_starter",     "Streak Starter",      "Maintain a 3-day streak",           "date_range",   3,  BadgeRuleType.STREAK_DAYS),
    BadgeDefinition("zone_scout",         "Zone Scout",          "Visit at least one zone this week", "place",        1,  BadgeRuleType.ZONES_VISITED),
)
