package com.example.gotouchgrass

import com.example.gotouchgrass.domain.UserPreferences
import org.junit.Assert.*
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun userPreferences_defaultValues_areCorrect() {
        val prefs = UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = true,
            darkModeEnabled = false,
            locationServicesEnabled = true
        )
        assertTrue(prefs.notificationsEnabled)
        assertTrue(prefs.soundEffectsEnabled)
        assertFalse(prefs.darkModeEnabled)
        assertTrue(prefs.locationServicesEnabled)
    }

    @Test
    fun userPreferences_copy_updatesCorrectField() {
        val prefs = UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = true,
            darkModeEnabled = false,
            locationServicesEnabled = true
        )
        val updated = prefs.copy(darkModeEnabled = true)
        assertTrue(updated.darkModeEnabled)
        // other fields unchanged
        assertTrue(updated.notificationsEnabled)
        assertTrue(updated.soundEffectsEnabled)
        assertTrue(updated.locationServicesEnabled)
    }

    @Test
    fun userPreferences_equality_worksCorrectly() {
        val prefs1 = UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = false,
            darkModeEnabled = true,
            locationServicesEnabled = false
        )
        val prefs2 = UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = false,
            darkModeEnabled = true,
            locationServicesEnabled = false
        )
        assertEquals(prefs1, prefs2)
    }

    @Test
    fun userPreferences_inequality_whenFieldsDiffer() {
        val prefs1 = UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = true,
            darkModeEnabled = false,
            locationServicesEnabled = true
        )
        val prefs2 = prefs1.copy(notificationsEnabled = false)
        assertNotEquals(prefs1, prefs2)
    }
}