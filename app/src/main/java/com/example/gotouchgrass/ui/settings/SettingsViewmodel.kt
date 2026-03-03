package com.example.gotouchgrass.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.domain.UserPreferences

class SettingsViewModel : ViewModel() {

    var preferences by mutableStateOf(
        UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = true,
            darkModeEnabled = false,
            locationServicesEnabled = true
        )
    )
        private set

    fun updatePreferences(updated: UserPreferences) {
        preferences = updated
    }

    // Account Settings - Simple string data
    val editProfileTitle = "Edit Profile"
    val notificationsTitle = "Notifications"
    val soundEffectsTitle = "Sound Effects"
    val darkModeTitle = "Dark Mode"

    // Privacy Settings
    val locationServicesTitle = "Location Services"
    val privacySettingsTitle = "Privacy Settings"

    // Support
    val helpCenterTitle = "Help Center"

    // Footer
    val appVersion = "Go Touch Grass v1.0.0"
    val appTagline = "Made with care for explorers everywhere"

    // Section Titles
    val accountSectionTitle = "ACCOUNT"
    val privacySectionTitle = "PRIVACY"
    val supportSectionTitle = "SUPPORT"

    // Button Text
    val logoutButtonText = "Log Out"
}