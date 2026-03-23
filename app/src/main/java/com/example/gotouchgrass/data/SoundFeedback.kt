package com.example.gotouchgrass.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import com.example.gotouchgrass.data.preferences.AppPreferencesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Short UI feedback tone; respects the sound-effects setting in [AppPreferencesStore].
 */
object SoundFeedback {

    fun playCaptureSuccess(context: Context) {
        runBlocking(Dispatchers.IO) {
            val enabled = AppPreferencesStore(context.applicationContext).readSoundEffectsEnabled()
            if (!enabled) return@runBlocking
            runCatching {
                val gen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                gen.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                Thread.sleep(200L)
                gen.release()
            }
        }
    }
}
