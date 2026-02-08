package com.example.gotouchgrass.ui.explore

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.R
import com.example.gotouchgrass.ui.theme.Error
import com.example.gotouchgrass.ui.theme.Info
import com.example.gotouchgrass.ui.theme.Success
import com.example.gotouchgrass.ui.theme.Warning

enum class Difficulty { EASY, MEDIUM, HARD, EXPERT }

enum class ChallengeType { VISIT, EXPLORE, TIME, ZONE, SOCIAL }

enum class RouteType { FOOD, DRINK, NATURE, CITY }

fun Difficulty.color(): Color = when (this) {
    Difficulty.EASY -> Success
    Difficulty.MEDIUM -> Info
    Difficulty.HARD -> Warning
    Difficulty.EXPERT -> Error
}

fun ChallengeType.iconRes(): Int = when (this) {
    ChallengeType.TIME -> R.drawable.schedule_24
    ChallengeType.ZONE -> R.drawable.target_24
    ChallengeType.VISIT -> R.drawable.location_on_24
    ChallengeType.SOCIAL -> R.drawable.stars_2_24
    ChallengeType.EXPLORE -> R.drawable.directions_walk_24
}

fun RouteType.iconRes(): Int = when (this) {
    RouteType.FOOD -> R.drawable.chef_hat_24
    RouteType.DRINK -> R.drawable.coffee_24
    RouteType.CITY -> R.drawable.location_city_24
    RouteType.NATURE -> R.drawable.nature_24
}

data class RouteCardData(
    val id: String,
    val title: String,
    val zoneCount: Number,
    val hours: Number,
    val description: String,
    val difficulty: Difficulty,
    val routeType: RouteType
)

data class ChallengeCardData(
    val id: String,
    val title: String,
    val description: String,
    val reward: Number,
    val progress: String,
    val progressFraction: Float,
    val challengeType: ChallengeType
)

class ExploreViewModel : ViewModel() {
    val dailyChallenges = listOf(
        ChallengeCardData(
            id = "0123",
            title = "Visit a New Zone",
            description = "Venture out and discover!",
            reward = 100,
            progress = "0 / 1",
            progressFraction = 0f,
            challengeType = ChallengeType.VISIT
        ),
        ChallengeCardData(
            id = "0124",
            title = "Explore 2km",
            description = "Go Touch Grass!",
            reward = 75,
            progress = "1.8 / 2",
            progressFraction = 0.9f,
            challengeType = ChallengeType.EXPLORE
        )
    )

    val weeklyChallenges = listOf(
        ChallengeCardData(
            id = "0125",
            title = "Zone Collector",
            description = "Visit 10 different zones",
            reward = 500,
            progress = "4 / 10",
            progressFraction = 0.4f,
            challengeType = ChallengeType.ZONE
        ),
        ChallengeCardData(
            id = "0126",
            title = "Social Explorer",
            description = "Visit 3 trending zones",
            reward = 300,
            progress = "2 / 3",
            progressFraction = 0.67f,
            challengeType = ChallengeType.SOCIAL
        )
    )

    val curatedRoutes = listOf(
        RouteCardData(
            id = "0127",
            title = "Coffee Trail",
            description = "Discover the best local cafes",
            zoneCount = 3,
            hours = 1.5,
            difficulty = Difficulty.EASY,
            routeType = RouteType.DRINK
        ),
        RouteCardData(
            id = "0128",
            title = "Nature Walk",
            description = "Parks, rivers, and green spaces nearby",
            zoneCount = 7,
            hours = 3,
            difficulty = Difficulty.MEDIUM,
            routeType = RouteType.NATURE
        )
    )
}