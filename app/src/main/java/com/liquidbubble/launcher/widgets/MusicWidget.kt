package com.liquidbubble.launcher.widgets

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.data.model.MediaPlaybackState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette

@Composable
fun MusicWidget(
    media: MediaPlaybackState,
    albumArt: Bitmap?,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onRequestAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    GlassWidget(modifier = modifier) {
        if (!media.hasNotificationListenerAccess) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onRequestAccess)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(palette.accentPrimary.copy(alpha = 0.25f))) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = IceWhite)
                }
                Column {
                    Text("Connect Media Player", color = IceWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Tap to enable Notification Access for live track controls", color = palette.textSecondary, fontSize = 12.sp)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(palette.accentSecondary.copy(alpha = 0.28f))) {
                    if (albumArt != null) {
                        Image(bitmap = albumArt.asImageBitmap(), contentDescription = "Album art", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = IceWhite)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(media.title, color = IceWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(media.artist, color = palette.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSkipPrevious, enabled = media.hasActiveSession) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = IceWhite) }
                    IconButton(
                        onClick = onTogglePlayPause,
                        enabled = media.hasActiveSession,
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f))
                    ) {
                        Icon(if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = "Play or pause", tint = IceWhite)
                    }
                    IconButton(onClick = onSkipNext, enabled = media.hasActiveSession) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = IceWhite) }
                }
            }
        }
    }
}
