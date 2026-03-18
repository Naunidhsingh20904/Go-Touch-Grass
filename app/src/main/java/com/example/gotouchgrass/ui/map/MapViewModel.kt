package com.example.gotouchgrass.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.MapHeaderStats
import com.example.gotouchgrass.domain.MapModel
import kotlinx.coroutines.launch

class MapViewModel(
    private val model: MapModel
) : ViewModel() {

    var headerStats by mutableStateOf(
        MapHeaderStats(
            level = 1,
            currentXp = 0,
            maxXp = 1000,
            xpToNextLevel = 1000,
            totalXp = 0,
            streakDays = 0,
            zonesVisited = 0,
            timeOutsideLabel = "0h"
        )
    )
        private set

    var nearbyRoutes by mutableStateOf<List<ExploreRouteItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                headerStats = model.getHeaderStats()
                nearbyRoutes = model.getNearbyRoutes()
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to load map data"
            }

            isLoading = false
        }
    }
}