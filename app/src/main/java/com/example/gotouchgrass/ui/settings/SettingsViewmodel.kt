package com.example.gotouchgrass.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.data.preferences.AppPreferencesStore
import com.example.gotouchgrass.domain.UserPreferences
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userId: String? = null,
    private val repository: GoTouchGrassRepository? = null,
    private val appPreferencesStore: AppPreferencesStore? = null
) : ViewModel() {

    var preferences by mutableStateOf(
        UserPreferences(
            notificationsEnabled = true,
            soundEffectsEnabled = true,
            darkModeEnabled = false,
            locationServicesEnabled = true
        )
    )
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val uid = userId ?: return
        val repo = repository ?: return
        viewModelScope.launch {
            repo.getUserSettings(uid).onSuccess { loadedPrefs ->
                preferences = loadedPrefs
                syncLocalStore(loadedPrefs)
            }
        }
    }

    private fun syncLocalStore(prefs: UserPreferences) {
        val store = appPreferencesStore ?: return
        viewModelScope.launch {
            store.setDarkMode(prefs.darkModeEnabled)
            store.setSoundEffectsEnabled(prefs.soundEffectsEnabled)
        }
    }

    fun updatePreferences(updated: UserPreferences) {
        preferences = updated
        syncLocalStore(updated)
        val uid = userId ?: return
        val repo = repository ?: return
        viewModelScope.launch {
            repo.saveUserSettings(uid, updated)
        }
    }

    val editProfileTitle = "Edit Profile"
    val notificationsTitle = "Notifications"
    val soundEffectsTitle = "Sound Effects"
    val darkModeTitle = "Dark Mode"
    val locationServicesTitle = "Location Services"
    val privacySettingsTitle = "Privacy Settings"
    val helpCenterTitle = "Help Center"
    val appVersion = "Go Touch Grass v1.0.0"
    val appTagline = "Made with care for explorers everywhere"
    val accountSectionTitle = "ACCOUNT"
    val privacySectionTitle = "PRIVACY"
    val supportSectionTitle = "SUPPORT"
    val logoutButtonText = "Log Out"
}
