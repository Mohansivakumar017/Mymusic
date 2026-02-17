package com.example.mymusic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager

/**
 * Helper class to manage media playback notifications
 */
@OptIn(UnstableApi::class)
class NotificationHelper(
    private val context: Context,
    private val player: Player,
    private val mediaSession: MediaSession
) {
    
    private var playerNotificationManager: PlayerNotificationManager? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "music_playback"
        private const val NOTIFICATION_CHANNEL_NAME = "Music Playback"
    }
    
    init {
        createNotificationChannel()
        setupNotificationManager()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows currently playing music"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun setupNotificationManager() {
        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(MusicDescriptionAdapter(context, mediaSession))
            .setSmallIconResourceId(R.drawable.ic_music_note)
            .build()
            .apply {
                setPlayer(player)
                setMediaSessionToken(mediaSession.sessionCompatToken)
                setUsePreviousAction(true)
                setUseNextAction(true)
                setUsePlayPauseActions(true)
            }
    }
    
    fun release() {
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null
    }
    
    /**
     * MediaDescriptionAdapter for notification
     */
    private class MusicDescriptionAdapter(
        private val context: Context,
        private val mediaSession: MediaSession
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.title ?: "Unknown Title"
        }
        
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        
        override fun getCurrentContentText(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.artist ?: "Unknown Artist"
        }
        
        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): android.graphics.Bitmap? {
            // Return null to use default icon
            return null
        }
    }
}
