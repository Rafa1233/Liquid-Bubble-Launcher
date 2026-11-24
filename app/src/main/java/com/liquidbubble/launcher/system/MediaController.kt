package com.liquidbubble.launcher.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.liquidbubble.launcher.data.model.MediaPlaybackState

class LauncherMediaController(private val context: Context) {

    private val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val listenerComponent = ComponentName(context, LiquidNotificationListenerService::class.java)
    private var activeController: MediaController? = null

    fun hasNotificationListenerAccess(): Boolean =
        context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)

    fun createNotificationListenerSettingsIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun refreshPlaybackState(): Pair<MediaPlaybackState, Bitmap?> {
        val mgr = sessionManager
        if (!hasNotificationListenerAccess() || mgr == null) {
            activeController = null
            return MediaPlaybackState(hasNotificationListenerAccess = false) to null
        }
        val controllers = runCatching { mgr.getActiveSessions(listenerComponent) }.getOrDefault(emptyList())
        val chosen = controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: controllers.firstOrNull()
        activeController = chosen
        if (chosen == null) return MediaPlaybackState(hasActiveSession = false, hasNotificationListenerAccess = true) to null

        val meta = chosen.metadata
        val playback = chosen.playbackState
        return MediaPlaybackState(
            hasActiveSession = true,
            packageName = chosen.packageName,
            title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE) ?: "Playing Media",
            artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: chosen.packageName,
            isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
            positionMs = (playback?.position ?: 0L).coerceAtLeast(0L),
            durationMs = (meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L).coerceAtLeast(0L),
            hasNotificationListenerAccess = true
        ) to (meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ART))
    }

    fun togglePlayPause() {
        val ctrl = activeController ?: return
        if (ctrl.playbackState?.state == PlaybackState.STATE_PLAYING) ctrl.transportControls.pause() else ctrl.transportControls.play()
    }
    fun skipToNext() { activeController?.transportControls?.skipToNext() }
    fun skipToPrevious() { activeController?.transportControls?.skipToPrevious() }
}
