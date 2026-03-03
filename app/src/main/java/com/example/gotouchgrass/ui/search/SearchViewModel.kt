package com.example.gotouchgrass.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.ui.theme.RarityCommon
import com.example.gotouchgrass.ui.theme.RarityEpic
import com.example.gotouchgrass.ui.theme.RarityLegendary
import com.example.gotouchgrass.ui.theme.RarityRare
import com.example.gotouchgrass.ui.theme.RarityUncommon
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }

fun Rarity.color(): Color = when (this) {
    Rarity.COMMON -> RarityCommon
    Rarity.UNCOMMON -> RarityUncommon
    Rarity.RARE -> RarityRare
    Rarity.EPIC -> RarityEpic
    Rarity.LEGENDARY -> RarityLegendary
}

data class LocationCardData(
    val id: String,
    val title: String,
    val description: String,
    val rarity: Rarity
)


class SearchViewModel : ViewModel() {
    private var placesClient: PlacesClient? = null
    private var currentOrigin: LatLng? = null
    private var searchJob: Job? = null

    var query by mutableStateOf("")
        private set

    var isSearching by mutableStateOf(false)
        private set

    var searchError by mutableStateOf<String?>(null)
        private set

    var searchResults by mutableStateOf<List<LocationCardData>>(emptyList())
        private set

    val recentSearches = listOf("DC Library", "Laurier University", "Lazeez")

    val trendingLocations = listOf(
        LocationCardData(
            id = "0123",
            title = "University Plaza",
            description = "37 active now",
            rarity = Rarity.COMMON
        ),
        LocationCardData(
            id = "0124",
            title = "Conestoga Mall",
            description = "72 active now",
            rarity = Rarity.COMMON
        )
    )

    val nearbyLocations = listOf(
        LocationCardData(
            id = "0125",
            title = "Library Square",
            description = "50m",
            rarity = Rarity.UNCOMMON
        ),
        LocationCardData(
            id = "0126",
            title = "Hidden Garden",
            description = "450m",
            rarity = Rarity.LEGENDARY
        )
    )

    fun onQueryChange(newQuery: String) {
        query = newQuery

        searchJob?.cancel()

        if (newQuery.trim().isBlank()) {
            searchResults = emptyList()
            searchError = null
            isSearching = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            onSearch()
        }
    }

    fun initPlaces(client: PlacesClient) {
        if (placesClient == null) {
            placesClient = client
        }
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        currentOrigin = LatLng(latitude, longitude)
    }

    fun onSearch() {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            searchResults = emptyList()
            searchError = null
            return
        }

        val client = placesClient
        if (client == null) {
            searchError = "Places is not initialized"
            return
        }

        isSearching = true
        searchError = null

        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(trimmedQuery)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
        currentOrigin?.let { requestBuilder.setOrigin(it) }

        val request = requestBuilder.build()

        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                val orderedPredictions = if (currentOrigin != null) {
                    predictions.sortedBy { prediction ->
                        prediction.distanceMeters ?: Int.MAX_VALUE
                    }
                } else {
                    predictions
                }

                searchResults = orderedPredictions.map { prediction ->
                    val distanceMeters = prediction.distanceMeters
                    val secondaryText = prediction.getSecondaryText(null).toString()
                    val distanceText = distanceMeters?.let { "${it}m away" }
                    val descriptionText = listOfNotNull(distanceText, secondaryText)
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")

                    LocationCardData(
                        id = prediction.placeId,
                        title = prediction.getPrimaryText(null).toString(),
                        description = descriptionText,
                        rarity = Rarity.COMMON
                    )
                }
                isSearching = false
            }
            .addOnFailureListener { exception ->
                searchResults = emptyList()
                searchError = exception.localizedMessage ?: "Search failed"
                isSearching = false
            }
    }
}