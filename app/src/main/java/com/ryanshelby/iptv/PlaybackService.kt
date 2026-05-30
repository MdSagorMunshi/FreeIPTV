package com.ryanshelby.iptv

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder

class PlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "playback_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_PLAY_PAUSE = "com.ryanshelby.iptv.ACTION_PLAY_PAUSE"
        const val ACTION_GO_LIVE = "com.ryanshelby.iptv.ACTION_GO_LIVE"

        private const val EXTRA_CHANNEL_NAME = "channel_name"
        private const val EXTRA_CHANNEL_GROUP = "channel_group"
        private const val EXTRA_IS_PLAYING = "is_playing"

        fun start(context: Context, channelName: String, channelGroup: String, isPlaying: Boolean) {
            val intent = Intent(context, PlaybackService::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_CHANNEL_GROUP, channelGroup)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PlaybackService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelName = intent?.getStringExtra(EXTRA_CHANNEL_NAME) ?: "FreeIPTV"
        val channelGroup = intent?.getStringExtra(EXTRA_CHANNEL_GROUP) ?: ""
        val isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, true) ?: true

        val notification = buildNotification(channelName, channelGroup, isPlaying)
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Now Playing",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Playback controls for the current channel"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(
        channelName: String,
        channelGroup: String,
        isPlaying: Boolean
    ): Notification {
        // Tap notification to open app
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause action
        val playPauseIntent = PendingIntent.getBroadcast(
            this, 100,
            Intent(ACTION_PLAY_PAUSE).apply { `package` = packageName },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIconRes = if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        // Go Live action — reloads the stream to get back to real-time
        val goLiveIntent = PendingIntent.getBroadcast(
            this, 101,
            Intent(ACTION_GO_LIVE).apply { `package` = packageName },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(channelName)
            .setContentText(if (channelGroup.isNotBlank()) "\uD83D\uDCFA $channelGroup" else "\uD83D\uDCFA Live TV")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setColor(0xFF6C5CE7.toInt()) // Purple40 accent
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, playPauseIconRes),
                    playPauseTitle, playPauseIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_popup_sync),
                    "Go Live", goLiveIntent
                ).build()
            )
            .setStyle(Notification.MediaStyle().setShowActionsInCompactView(0, 1))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
    }
}
