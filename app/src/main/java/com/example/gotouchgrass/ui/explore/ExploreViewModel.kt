package com.example.gotouchgrass.ui.explore

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.R
import com.example.gotouchgrass.domain.ChallengeType
import com.example.gotouchgrass.domain.ExploreChallengeItem
import com.example.gotouchgrass.domain.RouteTheme
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.ui.theme.Error
import com.example.gotouchgrass.ui.theme.Info
import com.example.gotouchgrass.ui.theme.Success
import com.example.gotouchgrass.ui.theme.Warning
import com.example.gotouchgrass.domain.ExploreModel
import com.example.gotouchgrass.domain.RouteDifficulty

enum class Difficulty { EASY, MEDIUM, HARD, EXPERT }

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

fun RouteTheme.iconRes(): Int = when (this) {
    RouteTheme.FOOD -> R.drawable.chef_hat_24
    RouteTheme.DRINKS -> R.drawable.coffee_24
    RouteTheme.PARKS -> R.drawable.nature_24
    RouteTheme.MURALS -> R.drawable.architecture_24dp
    RouteTheme.HIDDEN_STUDY_SPOTS -> R.drawable.book_2_24dp
    RouteTheme.CITY_HIGHLIGHTS -> R.drawable.location_city_24
}

data class RouteCardData(
    val id: String,
    val title: String,
    val zoneCount: Number,
    val hours: Number,
    val description: String,
    val difficulty: Difficulty,
    val theme: RouteTheme,
    val routeStops: List<String>
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

class ExploreViewModel(
    private val exploreModel: ExploreModel = ExploreModel(currentUserId = "user_you")
) : ViewModel() {

    val totalXP = exploreModel.getTotalXp()

    val dailyChallenges = exploreModel.getDailyChallenges().map { it.toChallengeCardData() }

    val weeklyChallenges = exploreModel.getWeeklyChallenges().map { it.toChallengeCardData() }

    val curatedRoutes = exploreModel.getCuratedRoutes().map { it.toRouteCardData() }
}

private fun ExploreChallengeItem.toChallengeCardData(): ChallengeCardData {
    return ChallengeCardData(
        id = id,
        title = title,
        description = description,
        reward = rewardXp,
        progress = progress,
        progressFraction = progressFraction,
        challengeType = challengeType
    )
}

private fun ExploreRouteItem.toRouteCardData(): RouteCardData {
    return RouteCardData(
        id = id,
        title = title,
        zoneCount = zoneCount,
        hours = hours,
        description = description,
        difficulty = when (difficulty) {
            RouteDifficulty.EASY -> Difficulty.EASY
            RouteDifficulty.MEDIUM -> Difficulty.MEDIUM
            RouteDifficulty.HARD -> Difficulty.HARD
        },
        theme = theme,
        routeStops = routeStops
    )
}