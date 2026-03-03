package com.example.gotouchgrass

import com.example.gotouchgrass.domain.SearchModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    @Test
    fun getRecentSearches_getsRecentSearches() {
        val model = SearchModel("user_you")

        val recent = model.getRecentSearches()

        assertEquals(3, recent.size)
        assertEquals("DC Library", recent.first())
    }

    @Test
    fun getSuggestedLocations_getsSuggestedLocations() {
        val model = SearchModel("user_you")

        val suggested = model.getSuggestedLocations()

        assertEquals(5, suggested.size)
        assertEquals("zone_dc_library", suggested.first().id)
    }

    @Test
    fun recordRecentSearch_updatesRecentSearches() {
        val model = SearchModel("user_you")
        model.recordRecentSearch("Waterloo Park")

        val recent = model.getRecentSearches()

        assertEquals("Waterloo Park", recent.first())
        assertTrue(recent.contains("DC Library"))
    }
}