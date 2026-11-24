package com.liquidbubble.launcher.wallpaper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.liquidbubble.launcher.data.model.PerformanceMode
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuraWallpaper(
    modifier: Modifier = Modifier,
    onWallpaperTap: (() -> Unit)? = null,
    onWallpaperDoubleTap: (() -> Unit)? = null,
    onWallpaperLongPress: (() -> Unit)? = null
) {
    val palette = LocalLiquidPalette.current
    val settings = LocalLiquidSettings.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var isResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> isResumed = event == Lifecycle.Event.ON_RESUME }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Only run continuous orb drift in ULTRA_VISUAL mode to keep 120 FPS locked on entry-level GPUs
    val animateOrbs = isResumed &&
        settings.performanceMode == PerformanceMode.ULTRA_VISUAL &&
        settings.auraMotionEnabled &&
        !settings.batterySaverMode &&
        !settings.reducedMotion

    var phaseTimeSec by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animateOrbs, settings.auraAnimationSpeed) {
        if (!animateOrbs) return@LaunchedEffect
        while (isActive) {
            delay(33L)
            phaseTimeSec += 0.033f * settings.auraAnimationSpeed.coerceIn(0.25f, 2.5f)
        }
    }

    var rippleCenter by remember { mutableStateOf<Offset?>(null) }
    val rippleProgress = remember { Animatable(0f) }

    val gestureModifier = Modifier.pointerInput(onWallpaperTap, onWallpaperDoubleTap, onWallpaperLongPress) {
        detectTapGestures(
            onTap = { pos ->
                rippleCenter = pos
                scope.launch { rippleProgress.snapTo(0f); rippleProgress.animateTo(1f, tween(520)) }
                onWallpaperTap?.invoke()
            },
            onDoubleTap = { onWallpaperDoubleTap?.invoke() },
            onLongPress = { onWallpaperLongPress?.invoke() }
        )
    }

    val orbScale = settings.auraOrbSize.coerceIn(0.6f, 1.5f)
    val glowStrength = settings.auraGlowStrength.coerceIn(0.2f, 1.3f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundBase)
            .then(gestureModifier)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val t = phaseTimeSec
                val c1 = Offset(w * (0.28f + 0.10f * sin(t * 0.4f)), h * (0.22f + 0.08f * cos(t * 0.3f)))
                val c2 = Offset(w * (0.76f + 0.10f * cos(t * 0.3f + 1.1f)), h * (0.50f + 0.10f * sin(t * 0.35f)))
                val c3 = Offset(w * (0.45f + 0.12f * sin(t * 0.25f + 2.2f)), h * (0.80f + 0.06f * cos(t * 0.4f)))
                val r1 = w * 0.62f * orbScale
                val r2 = w * 0.68f * orbScale
                val r3 = w * 0.56f * orbScale
                val b1 = Brush.radialGradient(listOf(palette.accentPrimary.copy(alpha = (0.52f * glowStrength).coerceIn(0.15f, 0.72f)), Color.Transparent), c1, r1)
                val b2 = Brush.radialGradient(listOf(palette.accentSecondary.copy(alpha = (0.46f * glowStrength).coerceIn(0.15f, 0.66f)), Color.Transparent), c2, r2)
                val b3 = Brush.radialGradient(listOf(palette.accentWarm.copy(alpha = (0.38f * glowStrength).coerceIn(0.12f, 0.56f)), Color.Transparent), c3, r3)

                onDrawBehind {
                    drawCircle(b1, r1, c1)
                    drawCircle(b2, r2, c2)
                    drawCircle(b3, r3, c3)
                    val rp = rippleProgress.value
                    val center = rippleCenter
                    if (center != null && rp > 0f && rp < 1f) {
                        drawCircle(Color.White.copy(alpha = (1f - rp) * 0.22f), w * 0.38f * rp, center, style = Stroke((1f - rp) * 5f + 1f))
                    }
                }
            }
    )
}
