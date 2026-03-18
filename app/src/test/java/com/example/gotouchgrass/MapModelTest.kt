package com.example.gotouchgrass

import com.example.gotouchgrass.data.FakeMapRepository
import com.example.gotouchgrass.data.FakeProfileRepository
import com.example.gotouchgrass.domain.MapModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapModelTest {

    private val model = MapModel(
        currentUserId = "user_you",
        profileRepository = FakeProfileRepository(),
        mapRepository = FakeMapRepository()
    )

    @Test
    fun headerStats_derivedFromMockProfileRepos() = runBlocking {
        val header = model.getHeaderStats()
        assertEquals(8, header.level)
        assertEquals(12450, header.totalXp)
        assertEquals(7, header.streakDays)
        assertEquals(5, header.zonesVisited)
        assertTrue(header.currentXp in 0..header.maxXp)
        assertEquals(header.maxXp - header.currentXp, header.xpToNextLevel)
    }

    @Test
    fun nearbyRoutes_comeFromMockMapRepo() = runBlocking {
        val routes = model.getNearbyRoutes()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.title.isNotBlank() })
    }
}

