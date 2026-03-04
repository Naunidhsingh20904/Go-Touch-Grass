package com.example.gotouchgrass

import com.example.gotouchgrass.domain.FakeData
import com.example.gotouchgrass.domain.ZoneType
import org.junit.Assert.*
import org.junit.Test

class MapTest {

    @Test
    fun fakeData_zones_haveValidIds() {
        assertTrue(FakeData.zones.all { it.id.isNotBlank() })
    }

    @Test
    fun fakeData_zones_haveValidCenterLocation() {
        assertTrue(FakeData.zones.all { it.centerLatLng.latitude != 0.0 })
        assertTrue(FakeData.zones.all { it.centerLatLng.longitude != 0.0 })
    }

    @Test
    fun fakeData_zones_containsCampusAndBuildings() {
        val hasCampus = FakeData.zones.any { it.type == ZoneType.CAMPUS }
        val hasBuilding = FakeData.zones.any { it.type == ZoneType.BUILDING }
        assertTrue(hasCampus)
        assertTrue(hasBuilding)
    }

    @Test
    fun fakeData_captures_isNotEmpty() {
        assertTrue(FakeData.captures.isNotEmpty())
    }

    @Test
    fun fakeData_captures_haveValidIds() {
        assertTrue(FakeData.captures.all { it.id.isNotBlank() })
    }

    @Test
    fun fakeData_captures_awardXp() {
        assertTrue(FakeData.captures.all { it.xpAwarded > 0 })
    }


    @Test
    fun fakeData_landmarks_isNotEmpty() {
        assertTrue(FakeData.landmarks.isNotEmpty())
    }

    @Test
    fun fakeData_landmarks_haveValidNames() {
        assertTrue(FakeData.landmarks.all { it.name.isNotBlank() })
    }
}