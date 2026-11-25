package com.liquidbubble.launcher.components

import android.graphics.Bitmap
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.animation.LiquidSprings
import com.liquidbubble.launcher.data.model.BatteryState
import com.liquidbubble.launcher.data.model.MediaPlaybackState
import com.liquidbubble.launcher.data.model.QuickControlsState
import com.liquidbubble.launcher.data.model.WeatherState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
import kotlin.math.roundToInt

@Composable
fun DynamicIsland(
    isExpanded: Boolean,
    weather: WeatherState,
    battery: BatteryState,
    media: MediaPlaybackState,
    albumArt: Bitmap?,
    quickControls: QuickControlsState,
    onToggleExpanded: (Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenWifiPanel: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalLiquidSettings.current
    if (!settings.dynamicIslandEnabled) return

    val palette = LocalLiquidPalette.current
    val view = LocalView.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp

    val collapsedWidth = (screenWidthDp * settings.islandWidthFraction.coerceIn(0.45f, 0.85f)).coerceIn(196.dp, 310.dp)
    val expandedWidth = (screenWidthDp - 24.dp).coerceIn(260.dp, 420.dp)

    val targetWidth by animateDpAsState(
        targetValue = if (isExpanded) expandedWidth else collapsedWidth,
        animationSpec = LiquidSprings.DynamicIslandDpSpec,
        label = "islandWidth"
    )
    // Safe corner range (22dp pill <-> 28dp card) so spring never overshoots negative or clips into a circle
    val targetCorner by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 22.dp,
        animationSpec = LiquidSprings.DynamicIslandDpSpec,
        label = "islandCorner"
    )

    val safeWidth = targetWidth.coerceAtLeast(180.dp)
    val safeCorner = targetCorner.coerceAtLeast(16.dp)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(top = 6.dp)
    ) {
        LiquidBubbleSurface(
            shape = RoundedCornerShape(safeCorner),
            tintColorOverride = Color(0xFF06080F),
            glassAlphaOverride = 0.88f,
            glowColorOverride = palette.accentPrimary,
            borderWidth = 1.25.dp,
            modifier = Modifier
                .width(safeWidth)
                .clickable(enabled = !isExpanded) {
                    if (settings.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onToggleExpanded(true)
                }
                .pointerInput(isExpanded) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (isExpanded && dragAmount < -18f) onToggleExpanded(false)
                        else if (!isExpanded && dragAmount > 18f) onToggleExpanded(true)
                    }
                }
                .semantics { contentDescription = "Dynamic Bubble Island" }
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "islandContent"
            ) { expanded ->
                if (!expanded) {
                    CollapsedIslandContent(
                        weather = weather, battery = battery, media = media,
                        showWeather = settings.islandWeatherVisible,
                        showBattery = settings.islandBatteryVisible,
                        showMusic = settings.islandMusicVisible
                    )
                } else {
                    ExpandedIslandContent(
                        media = media, albumArt = albumArt, quickControls = quickControls,
                        onCollapse = { onToggleExpanded(false) },
                        onTogglePlayPause = onTogglePlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious,
                        onRequestNotificationAccess = onRequestNotificationAccess,
                        onToggleTorch = onToggleTorch, onOpenWifiPanel = onOpenWifiPanel,
                        onOpenBluetoothSettings = onOpenBluetoothSettings, onBrightnessChange = onBrightnessChange
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsedIslandContent(
    weather: WeatherState, battery: BatteryState, media: MediaPlaybackState,
    showWeather: Boolean, showBattery: Boolean, showMusic: Boolean
) {
    val palette = LocalLiquidPalette.current
    val settings = LocalLiquidSettings.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().height(settings.islandHeightDp.dp.coerceIn(34.dp, 44.dp)).padding(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Brush.radialGradient(listOf(IceWhite, palette.accentPrimary, palette.accentSecondary))))
            if (showWeather) {
                val tempStr = weather.temperatureCelsius?.let { "${it.roundToInt()}°" } ?: "--°"
                val statusStr = if (weather.temperatureCelsius != null) weather.conditionLabel else weather.locationName
                Text("$tempStr $statusStr", color = IceWhite, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showMusic && media.isPlaying) MiniWaveform(isPlaying = true)
            if (showBattery && battery.levelPercent >= 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        imageVector = if (battery.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                        contentDescription = "Battery",
                        tint = if (battery.isCharging) Color(0xFF4ADE80) else palette.rimCyanTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("${battery.levelPercent}%", color = IceWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ExpandedIslandContent(
    media: MediaPlaybackState, albumArt: Bitmap?, quickControls: QuickControlsState,
    onCollapse: () -> Unit, onTogglePlayPause: () -> Unit, onSkipNext: () -> Unit, onSkipPrevious: () -> Unit,
    onRequestNotificationAccess: () -> Unit, onToggleTorch: () -> Unit,
    onOpenWifiPanel: () -> Unit, onOpenBluetoothSettings: () -> Unit, onBrightnessChange: (Float) -> Unit
) {
    val palette = LocalLiquidPalette.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Dynamic Island", color = palette.rimCyanTint, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Tutup", color = IceWhite.copy(alpha = 0.80f), fontSize = 11.5.sp,
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable(onClick = onCollapse).padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        if (!media.hasNotificationListenerAccess) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.07f)).clickable { onRequestNotificationAccess() }.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = palette.rimCyanTint)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Sinkronkan Pemutar Musik", color = IceWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Ketuk untuk izinkan akses notifikasi musik", color = IceWhite.copy(alpha = 0.68f), fontSize = 11.sp)
                }
            }
        } else if (!media.hasActiveSession) {
            Row(
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.06f)).padding(12.dp)
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = palette.textSecondary)
                Column {
                    Text("Tidak Ada Musik Aktif", color = IceWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Putar lagu di Spotify, YT Music, atau pemutar lain", color = IceWhite.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(palette.accentPrimary.copy(alpha = 0.28f))) {
                        if (albumArt != null) Image(bitmap = albumArt.asImageBitmap(), contentDescription = "Album art", contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                        else Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = IceWhite)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(media.title, color = IceWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(media.artist, color = IceWhite.copy(alpha = 0.72f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    MiniWaveform(isPlaying = media.isPlaying)
                }
                if (media.durationMs > 0L) {
                    val progress = (media.positionMs.toFloat() / media.durationMs.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { progress }, color = palette.accentPrimary, trackColor = Color.White.copy(alpha = 0.14f), modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape))
                }
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onSkipPrevious) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous track", tint = IceWhite) }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f))) {
                        Icon(if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (media.isPlaying) "Pause" else "Play", tint = IceWhite)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onSkipNext) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next track", tint = IceWhite) }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickTile(if (quickControls.isTorchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff, if (quickControls.isTorchOn) "Senter On" else "Senter", quickControls.isTorchOn, onToggleTorch, Modifier.weight(1f))
            QuickTile(Icons.Rounded.Wifi, if (quickControls.isWifiConnected) "Wi-Fi On" else "Wi-Fi", quickControls.isWifiConnected, onOpenWifiPanel, Modifier.weight(1f))
            QuickTile(Icons.Rounded.Bluetooth, if (quickControls.isBluetoothOn) "BT On" else "Bluetooth", quickControls.isBluetoothOn, onOpenBluetoothSettings, Modifier.weight(1f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Rounded.BrightnessMedium, contentDescription = "Kecerahan", tint = IceWhite, modifier = Modifier.size(18.dp))
            val sliderVal = if (quickControls.windowBrightness < 0f) 0.65f else quickControls.windowBrightness
            Slider(
                value = sliderVal, onValueChange = onBrightnessChange, valueRange = 0.08f..1.0f,
                colors = SliderDefaults.colors(thumbColor = IceWhite, activeTrackColor = palette.accentPrimary, inactiveTrackColor = Color.White.copy(alpha = 0.18f)),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickTile(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    val bg = if (isActive) palette.accentPrimary.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.09f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(bg)
            .border(1.dp, if (isActive) Color.White.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = IceWhite, modifier = Modifier.size(20.dp))
        Text(label, color = IceWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MiniWaveform(isPlaying: Boolean) {
    val palette = LocalLiquidPalette.current
    val transition = rememberInfiniteTransition(label = "waveform")
    val bar1 by transition.animateFloat(0.35f, 1.0f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "b1")
    val bar2 by transition.animateFloat(0.9f, 0.25f, infiniteRepeatable(tween(340), RepeatMode.Reverse), label = "b2")
    val bar3 by transition.animateFloat(0.45f, 0.95f, infiniteRepeatable(tween(510), RepeatMode.Reverse), label = "b3")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(14.dp)) {
        listOf(bar1, bar2, bar3).forEach { level ->
            Box(modifier = Modifier.width(2.5.dp).height((14f * if (isPlaying) level else 0.35f).dp).clip(CircleShape).background(palette.rimCyanTint))
        }
    }
}
