package com.example.gotouchgrass.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object AppNotificationHelper {

    const val DEFAULT_CHANNEL_ID = "go_touch_grass_general"

    fun ensureDefaultChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(DEFAULT_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Exploration reminders and updates"
        }
        manager.createNotificationChannel(channel)
    }
}
