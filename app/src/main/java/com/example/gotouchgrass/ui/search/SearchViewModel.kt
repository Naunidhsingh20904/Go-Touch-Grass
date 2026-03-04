package com.example.gotouchgrass.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.domain.LatLng as DomainLatLng
import com.example.gotouchgrass.domain.Rarity
import com.example.gotouchgrass.domain.SearchLocation
import com.example.gotouchgrass.domain.SearchModel
import com.example.gotouchgrass.ui.theme.RarityCommon
import com.example.gotouchgrass.ui.theme.RarityEpic
import com.example.gotouchgrass.ui.theme.RarityLegendary
import com.example.gotouchgrass.ui.theme.RarityRare
import com.example.gotouchgrass.ui.theme.RarityUncommon
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private val searchModel = SearchModel(currentUserId = "user_you")
    private var placesClient: PlacesClient? = null
    private var currentOrigin: DomainLatLng? = null
    private var searchJob: Job? = null

    var query by mutableStateOf("")
        private set

    var isSearching by mutableStateOf(false)
        private set

    var searchError by mutableStateOf<String?>(null)
        private set

    var searchResults by mutableStateOf<List<LocationCardData>>(emptyList())
        private set

    var recentSearches by mutableStateOf(searchModel.getRecentSearches())
        private set

    var trendingLocations by mutableStateOf(
        searchModel.getSuggestedLocations().map { it.toCardData() }
    )
        private set

    fun initPlaces(client: PlacesClient) {
        if (placesClient == null) {
            placesClient = client
        }
    }

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

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        currentOrigin = DomainLatLng(latitude, longitude)
    }

    fun onSearch() {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            searchResults = emptyList()
            searchError = null
            return
        }

        searchModel.recordRecentSearch(trimmedQuery)
        recentSearches = searchModel.getRecentSearches()

        val client = placesClient
        if (client == null) {
            searchResults = emptyList()
            searchError = "Places is not initialized"
            return
        }

        isSearching = true
        searchError = null

        val request = buildAutocompleteRequest(trimmedQuery)

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

                searchResults = orderedPredictions.map { it.toLocationCardData() }
                isSearching = false
            }
            .addOnFailureListener { exception ->
                searchResults = emptyList()
                searchError = exception.localizedMessage ?: "Search failed"
                isSearching = false
            }
    }

    private fun buildAutocompleteRequest(query: String): FindAutocompletePredictionsRequest {
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)

        currentOrigin?.let { requestBuilder.setOrigin(it.toGoogleLatLng()) }
        return requestBuilder.build()
    }

    private fun AutocompletePrediction.toLocationCardData(): LocationCardData {
        val distanceText = distanceMeters?.let { "${it}m away" }
        val secondaryText = getSecondaryText(null).toString()
        val descriptionText = listOfNotNull(distanceText, secondaryText)
            .filter { it.isNotBlank() }
            .joinToString(" • ")

        return LocationCardData(
            id = placeId,
            title = getPrimaryText(null).toString(),
            description = descriptionText,
            rarity = Rarity.COMMON
        )
    }
}

private fun SearchLocation.toCardData(): LocationCardData {
    return LocationCardData(
        id = id,
        title = title,
        description = description,
        rarity = rarity
    )
}

private fun DomainLatLng.toGoogleLatLng(): LatLng {
    return LatLng(latitude, longitude)
}