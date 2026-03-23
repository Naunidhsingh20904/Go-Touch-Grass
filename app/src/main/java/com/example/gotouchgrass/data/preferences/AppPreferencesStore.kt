package com.example.gotouchgrass.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.goTouchGrassAppDataStore by preferencesDataStore(name = "go_touch_grass_app")

private object Keys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
}

/**
 * Local preferences for instant UI (theme, sound) without waiting on the network.
 */
class AppPreferencesStore(context: Context) {

    private val dataStore = context.applicationContext.goTouchGrassAppDataStore

    val darkModeFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.DARK_MODE] ?: false }

    val soundEffectsFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.SOUND_EFFECTS] ?: true }

    suspend fun readDarkMode(): Boolean = darkModeFlow.first()

    suspend fun readSoundEffectsEnabled(): Boolean = soundEffectsFlow.first()

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }
}
