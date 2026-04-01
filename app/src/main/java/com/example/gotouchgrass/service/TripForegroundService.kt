package com.example.gotouchgrass.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.gotouchgrass.MainActivity
import com.example.gotouchgrass.R

class TripForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "trip_active_channel"
        private const val NOTIFICATION_ID = 1001
        const val EXTRA_START_MS = "start_ms"

        fun buildStartIntent(context: Context, startMs: Long): Intent =
            Intent(context, TripForegroundService::class.java).apply {
                putExtra(EXTRA_START_MS, startMs)
            }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var tripStartMs = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, 30_000L) // refresh every 30s
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tripStartMs = intent?.getLongExtra(EXTRA_START_MS, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, buildNotification(elapsedMinutes()))
        handler.post(tickRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tickRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun elapsedMinutes(): Long {
        val elapsedMs = System.currentTimeMillis() - tripStartMs
        return (elapsedMs / 60_000L).coerceAtLeast(0L)
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(elapsedMinutes()))
    }

    private fun buildNotification(minutes: Long): android.app.Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val elapsed = if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Trip Active")
            .setContentText("You've been exploring for $elapsed — keep going!")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Active Trip",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows while a trip is in progress"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
