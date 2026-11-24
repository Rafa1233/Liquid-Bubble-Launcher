package com.liquidbubble.launcher.wallpaper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.liquidbubble.launcher.data.model.PerformanceMode
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var isResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val animateOrbs = isResumed &&
        settings.auraMotionEnabled &&
        !settings.batterySaverMode &&
        !settings.reducedMotion &&
        settings.performanceMode != PerformanceMode.PERFORMANCE

    var phaseTimeSec by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animateOrbs, settings.auraAnimationSpeed) {
        if (!animateOrbs) return@LaunchedEffect
        var lastFrameMs = -1L
        while (isActive) {
            withFrameMillis { frameMs ->
                if (lastFrameMs >= 0L) {
                    val dt = ((frameMs - lastFrameMs).coerceIn(0L, 64L) / 1000f)
                    phaseTimeSec += dt * settings.auraAnimationSpeed.coerceIn(0.25f, 2.5f)
                }
                lastFrameMs = frameMs
            }
        }
    }

    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    DisposableEffect(context, animateOrbs) {
        if (!animateOrbs) {
            tiltX = 0f; tiltY = 0f
            return@DisposableEffect onDispose {}
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || gravitySensor == null) return@DisposableEffect onDispose {}

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val nx = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
                val ny = (event.values[1] / 9.81f).coerceIn(-1f, 1f)
                tiltX = tiltX * 0.90f + nx * 0.10f
                tiltY = tiltY * 0.90f + ny * 0.10f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, gravitySensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    var rippleCenter by remember { mutableStateOf<Offset?>(null) }
    val rippleProgress = remember { Animatable(0f) }

    val gestureModifier = Modifier.pointerInput(onWallpaperTap, onWallpaperDoubleTap, onWallpaperLongPress) {
        detectTapGestures(
            onTap = { pos ->
                rippleCenter = pos
                scope.launch {
                    rippleProgress.snapTo(0f)
                    rippleProgress.animateTo(1f, tween(durationMillis = 680))
                }
                onWallpaperTap?.invoke()
            },
            onDoubleTap = { onWallpaperDoubleTap?.invoke() },
            onLongPress = { onWallpaperLongPress?.invoke() }
        )
    }

    val orbScale = settings.auraOrbSize.coerceIn(0.6f, 1.5f)
    val glowStrength = settings.auraGlowStrength.coerceIn(0.2f, 1.3f)

    Box(modifier = modifier.fillMaxSize().background(palette.backgroundBase).then(gestureModifier)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = phaseTimeSec
            val parallaxX = tiltX * w * 0.035f
            val parallaxY = tiltY * h * 0.025f

            val cobaltCenter = Offset(
                w * (0.28f + 0.14f * sin(t * 0.34f) + 0.05f * cos(t * 0.71f)) + parallaxX,
                h * (0.24f + 0.10f * cos(t * 0.29f) + 0.04f * sin(t * 0.63f)) + parallaxY
            )
            val cobaltRadius = w * 0.62f * orbScale
            val cobaltAlpha = (0.48f + 0.12f * sin(t * 0.55f)) * glowStrength.coerceAtMost(1.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.accentPrimary.copy(alpha = cobaltAlpha.coerceIn(0.15f, 0.72f)),
                        palette.accentPrimary.copy(alpha = (cobaltAlpha * 0.35f).coerceIn(0.05f, 0.32f)),
                        Color.Transparent
                    ),
                    center = cobaltCenter, radius = cobaltRadius
                ),
                radius = cobaltRadius, center = cobaltCenter
            )

            val violetCenter = Offset(
                w * (0.74f + 0.13f * cos(t * 0.27f + 1.1f) + 0.05f * sin(t * 0.59f)) - parallaxX * 0.8f,
                h * (0.50f + 0.14f * sin(t * 0.31f + 0.6f)) + parallaxY * 0.7f
            )
            val violetRadius = w * 0.68f * orbScale
            val violetAlpha = (0.44f + 0.11f * cos(t * 0.47f)) * glowStrength.coerceAtMost(1.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.accentSecondary.copy(alpha = violetAlpha.coerceIn(0.15f, 0.68f)),
                        palette.accentSecondary.copy(alpha = (violetAlpha * 0.32f).coerceIn(0.05f, 0.28f)),
                        Color.Transparent
                    ),
                    center = violetCenter, radius = violetRadius
                ),
                radius = violetRadius, center = violetCenter
            )

            val coralCenter = Offset(
                w * (0.46f + 0.18f * sin(t * 0.22f + 2.4f) - 0.06f * cos(t * 0.52f)) + parallaxX * 0.6f,
                h * (0.78f + 0.09f * cos(t * 0.37f + 1.7f)) - parallaxY * 0.9f
            )
            val coralRadius = w * 0.56f * orbScale
            val coralAlpha = (0.38f + 0.10f * sin(t * 0.62f + 1.2f)) * glowStrength.coerceAtMost(1.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.accentWarm.copy(alpha = coralAlpha.coerceIn(0.12f, 0.58f)),
                        palette.accentWarm.copy(alpha = (coralAlpha * 0.28f).coerceIn(0.04f, 0.24f)),
                        Color.Transparent
                    ),
                    center = coralCenter, radius = coralRadius
                ),
                radius = coralRadius, center = coralCenter
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.16f), Color.Transparent, Color.Black.copy(alpha = 0.22f))
                )
            )

            val rp = rippleProgress.value
            val center = rippleCenter
            if (center != null && rp > 0f && rp < 1f) {
                val currentR = w * 0.42f * rp
                drawCircle(
                    color = Color.White.copy(alpha = (1f - rp) * 0.22f),
                    radius = currentR,
                    center = center,
                    style = Stroke(width = (1f - rp) * 6f + 1f)
                )
            }
        }
    }
}
