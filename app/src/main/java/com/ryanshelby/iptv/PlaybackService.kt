package com.ryanshelby.iptv

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.os.Build
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

    private var largeIcon: Bitmap? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Pre-load and round the app icon for the large notification icon
        largeIcon = createRoundedLargeIcon()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelName = intent?.getStringExtra(EXTRA_CHANNEL_NAME) ?: "FreeIPTV"
        val channelGroup = intent?.getStringExtra(EXTRA_CHANNEL_GROUP) ?: ""
        val isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, true) ?: true

        val notification = buildNotification(channelName, channelGroup, isPlaying)
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        largeIcon?.recycle()
        largeIcon = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Now Playing",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Playback controls for the current channel"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Creates a rounded large icon from the app's launcher icon for a polished look.
     */
    private fun createRoundedLargeIcon(): Bitmap {
        val drawable = androidx.core.content.ContextCompat.getDrawable(this, R.mipmap.ic_launcher)
        val size = 128
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        
        if (drawable != null) {
            val canvas = Canvas(output)
            val path = android.graphics.Path().apply {
                addRoundRect(RectF(0f, 0f, size.toFloat(), size.toFloat()), size / 2f, size / 2f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(path)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
        }
        return output
    }

    private fun buildNotification(
        channelName: String,
        channelGroup: String,
        isPlaying: Boolean
    ): Notification {
        // ── Content intent: tap notification to return to app ──
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Action: Play / Pause ──
        val playPauseIntent = PendingIntent.getBroadcast(
            this, 100,
            Intent(ACTION_PLAY_PAUSE).apply { `package` = packageName },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIconRes = if (isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play
        val playPauseLabel = if (isPlaying) "Pause" else "Play"

        // ── Action: Go Live (reload stream to real-time) ──
        val goLiveIntent = PendingIntent.getBroadcast(
            this, 101,
            Intent(ACTION_GO_LIVE).apply { `package` = packageName },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Build subtitle ──
        val subtitle = buildString {
            if (isPlaying) append("LIVE") else append("Paused")
            if (channelGroup.isNotBlank()) {
                append("  ·  ")
                append(channelGroup)
            }
        }

        // ── Notification ──
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif)
            .setLargeIcon(largeIcon)
            .setContentTitle(channelName)
            .setContentText(subtitle)
            .setSubText("FreeIPTV")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setColor(0xFF6C5CE7.toInt()) // Purple40 brand accent
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, playPauseIconRes),
                    playPauseLabel, playPauseIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_notif_live),
                    "Go Live", goLiveIntent
                ).build()
            )
            .setStyle(
                Notification.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )

        // Ensure notification appears immediately (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return builder.build()
    }
}
