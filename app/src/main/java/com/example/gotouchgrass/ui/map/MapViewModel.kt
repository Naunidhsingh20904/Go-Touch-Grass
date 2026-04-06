package com.example.gotouchgrass.ui.map

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.domain.ExploreRouteItem
import com.example.gotouchgrass.domain.FriendMapMarker
import com.example.gotouchgrass.domain.MapHeaderStats
import com.example.gotouchgrass.domain.MapModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(
    private val model: MapModel,
    private val repository: GoTouchGrassRepository? = null,
    private val currentUserId: String? = null
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

    var hasFocusedOnUserLocation by mutableStateOf(false)
        private set

    var savedCameraTarget by mutableStateOf<LatLng?>(null)
        private set

    var savedCameraZoom by mutableStateOf<Float?>(null)
        private set

    var friendLocations by mutableStateOf<List<FriendMapMarker>>(emptyList())
        private set

    init {
        refresh()
        loadFriendLocations()
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

    fun markUserLocationFocused() {
        hasFocusedOnUserLocation = true
    }

    fun saveCameraPosition(target: LatLng, zoom: Float) {
        savedCameraTarget = target
        savedCameraZoom = zoom
    }

    fun loadFriendLocations() {
        val repo = repository ?: return
        val uid = currentUserId ?: return
        viewModelScope.launch {
            repo.getFriendsApproxLocations(uid)
                .onSuccess { markers ->
                    Log.d("MapVM", "Friend locations loaded: ${markers.size} markers")
                    friendLocations = markers
                }
                .onFailure { error ->
                    Log.e("MapVM", "Failed to load friend locations", error)
                }
        }
    }

    // Callback set by MapScreen so the NearbyAreasOverlay "Start" button can kick off a route trip
    var onStartRoute: ((com.example.gotouchgrass.domain.ExploreRouteItem) -> Unit)? = null
}