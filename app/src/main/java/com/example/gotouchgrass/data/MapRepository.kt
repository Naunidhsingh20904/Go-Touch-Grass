package com.example.gotouchgrass.data

import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.RouteDifficulty
import com.example.gotouchgrass.domain.RouteTheme

/**
 * Map‑focused data access interface for "nearby" content shown on the Map page.
 *
 * The domain/model layer depends on this interface so we can swap the real DB
 * implementation with a deterministic fake for unit tests.
 */
interface MapRepository {
    suspend fun getNearbyRoutes(): Result<List<ExploreRouteItem>>
}

/**
 * Real implementation backed by [GoTouchGrassRepository] / Supabase.
 *
 * Today this maps "nearby areas" to the same curated routes used by Explore.
 */
class SupabaseMapRepository(
    private val repository: GoTouchGrassRepository
) : MapRepository {
    override suspend fun getNearbyRoutes(): Result<List<ExploreRouteItem>> = repository.getCuratedRoutes()
}

/**
 * Mock implementation that mimics "nearby routes" using [FakeData].
 */
class FakeMapRepository : MapRepository {
    override suspend fun getNearbyRoutes(): Result<List<ExploreRouteItem>> = runCatching {
        val stopsByRouteId = FakeData.routeStops.groupBy { it.routeId }

        FakeData.routes.map { route ->
            val stops = (stopsByRouteId[route.id] ?: emptyList()).sortedBy { it.orderIndex }
            val stopStrings = stops.mapIndexed { idx, stop ->
                if (!stop.hintText.isNullOrBlank()) "${idx + 1}. ${stop.hintText}"
                else "Stop ${idx + 1}"
            }

            ExploreRouteItem(
                id = route.id,
                title = route.title,
                zoneCount = stops.size,
                hours = route.estimatedDurationMinutes / 60.0,
                description = route.theme.toRouteDescription(),
                difficulty = route.difficulty,
                theme = route.theme,
                routeStops = stopStrings
            )
        }
    }
}

private fun RouteTheme.toRouteDescription(): String {
    return when (this) {
        RouteTheme.FOOD -> "Discover local food spots"
        RouteTheme.DRINKS -> "Find your next drink stop"
        RouteTheme.PARKS -> "Parks and green spaces nearby"
        RouteTheme.MURALS -> "Street art and mural highlights"
        RouteTheme.HIDDEN_STUDY_SPOTS -> "Quiet and hidden study places"
        RouteTheme.CITY_HIGHLIGHTS -> "Top city highlights to explore"
    }
}

