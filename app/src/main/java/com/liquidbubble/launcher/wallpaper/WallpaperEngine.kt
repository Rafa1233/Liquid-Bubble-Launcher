package com.liquidbubble.launcher.wallpaper

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.liquidbubble.launcher.data.model.WallpaperMode
import com.liquidbubble.launcher.data.model.WallpaperScaleMode
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings

data class BuiltInWallpaperPreset(
    val id: Int,
    val name: String,
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val glowColor: Color
)

val BuiltInWallpapers = listOf(
    BuiltInWallpaperPreset(0, "Titanium Indigo", Color(0xFF070913), Color(0xFF1E295E), Color(0xFF0D1126), Color(0xFF3B82F6)),
    BuiltInWallpaperPreset(1, "Spatial Aurora", Color(0xFF09051A), Color(0xFF3B1D7A), Color(0xFF0A2E47), Color(0xFF2DD4BF)),
    BuiltInWallpaperPreset(2, "Obsidian Rose", Color(0xFF140710), Color(0xFF4C1334), Color(0xFF16112B), Color(0xFFF43F5E)),
    BuiltInWallpaperPreset(3, "Glacier Silver", Color(0xFF101928), Color(0xFF2D4363), Color(0xFF0D1520), Color(0xFFA5B4FC)),
    BuiltInWallpaperPreset(4, "Deep Cobalt", Color(0xFF030B1A), Color(0xFF0F3460), Color(0xFF071326), Color(0xFF38BDF8)),
    BuiltInWallpaperPreset(5, "Solar Dusk", Color(0xFF14090E), Color(0xFF511C38), Color(0xFF1E1230), Color(0xFFFB923C))
)

@Composable
fun WallpaperEngine(
    modifier: Modifier = Modifier,
    onWallpaperDoubleTap: (() -> Unit)? = null,
    onWallpaperLongPress: (() -> Unit)? = null
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    var customImageFailed by remember(settings.customWallpaperUri) { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            settings.wallpaperMode == WallpaperMode.CUSTOM_IMAGE &&
                !settings.customWallpaperUri.isNullOrBlank() &&
                !customImageFailed -> {
                val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && settings.wallpaperBlurDp > 0.5f) {
                    Modifier.blur(settings.wallpaperBlurDp.dp)
                } else Modifier

                val scale = when (settings.wallpaperScaleMode) {
                    WallpaperScaleMode.CROP -> ContentScale.Crop
                    WallpaperScaleMode.FIT -> ContentScale.Fit
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.backgroundBase)
                        .pointerInput(onWallpaperDoubleTap, onWallpaperLongPress) {
                            detectTapGestures(
                                onDoubleTap = { onWallpaperDoubleTap?.invoke() },
                                onLongPress = { onWallpaperLongPress?.invoke() }
                            )
                        }
                ) {
                    AsyncImage(
                        model = Uri.parse(settings.customWallpaperUri),
                        contentDescription = "Custom launcher wallpaper",
                        contentScale = scale,
                        onError = { customImageFailed = true },
                        modifier = Modifier.fillMaxSize().then(blurModifier)
                    )
                }
            }

            settings.wallpaperMode == WallpaperMode.BUILT_IN_PRESET -> {
                val preset = BuiltInWallpapers.getOrElse(settings.builtInWallpaperIndex) { BuiltInWallpapers.first() }
                BuiltInMeshWallpaper(preset, onWallpaperDoubleTap, onWallpaperLongPress)
            }

            else -> {
                AuraWallpaper(onWallpaperDoubleTap = onWallpaperDoubleTap, onWallpaperLongPress = onWallpaperLongPress)
            }
        }

        if (settings.wallpaperDimAlpha > 0.01f) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = settings.wallpaperDimAlpha.coerceIn(0f, 0.75f))))
        }
    }
}

@Composable
private fun BuiltInMeshWallpaper(
    preset: BuiltInWallpaperPreset,
    onDoubleTap: (() -> Unit)?,
    onLongPress: (() -> Unit)?
) {
    Canvas(
        modifier = Modifier.fillMaxSize().pointerInput(onDoubleTap, onLongPress) {
            detectTapGestures(onDoubleTap = { onDoubleTap?.invoke() }, onLongPress = { onLongPress?.invoke() })
        }
    ) {
        drawRect(brush = Brush.verticalGradient(colors = listOf(preset.topColor, preset.midColor, preset.bottomColor)))
        drawCircle(
            brush = Brush.radialGradient(colors = listOf(preset.glowColor.copy(alpha = 0.45f), Color.Transparent), center = Offset(size.width * 0.65f, size.height * 0.30f), radius = size.width * 0.78f),
            radius = size.width * 0.78f, center = Offset(size.width * 0.65f, size.height * 0.30f)
        )
    }
}
