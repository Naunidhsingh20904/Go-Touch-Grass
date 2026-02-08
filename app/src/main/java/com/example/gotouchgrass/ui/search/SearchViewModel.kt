package com.example.gotouchgrass.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.ui.theme.RarityCommon
import com.example.gotouchgrass.ui.theme.RarityEpic
import com.example.gotouchgrass.ui.theme.RarityLegendary
import com.example.gotouchgrass.ui.theme.RarityRare
import com.example.gotouchgrass.ui.theme.RarityUncommon

import androidx.compose.ui.graphics.Color

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
    var query by mutableStateOf("")
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
    }

    fun onSearch() {}
}