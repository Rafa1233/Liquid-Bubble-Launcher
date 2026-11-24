package com.liquidbubble.launcher.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liquidbubble.launcher.data.model.PerformanceMode
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings

@Composable
fun LiquidBubbleSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    glassAlphaOverride: Float? = null,
    blurDpOverride: Dp? = null,
    borderWidth: Dp = 1.25.dp,
    tintColorOverride: Color? = null,
    glowColorOverride: Color? = null,
    enableGlow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalLiquidPalette.current
    val settings = LocalLiquidSettings.current
    val density = LocalDensity.current

    val effectiveAlpha = (glassAlphaOverride ?: settings.glassAlpha).coerceIn(0.14f, 0.88f)
    val effectiveBaseColor = tintColorOverride ?: palette.glassBase
    val effectiveGlowColor = glowColorOverride ?: palette.accentPrimary
    val effectiveBorderAlphaScale = settings.borderIntensity.coerceIn(0.2f, 1.2f)
    val effectiveGlowStrength = if (enableGlow) settings.glowStrength.coerceIn(0f, 1f) else 0f

    val blurRadiusPx = if (settings.performanceMode == PerformanceMode.ULTRA_VISUAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        with(density) { (blurDpOverride ?: settings.glassBlurDp.dp).coerceAtMost(16.dp).toPx() }
    } else 0f

    val frostComposeEffect = remember(blurRadiusPx) {
        if (blurRadiusPx > 1f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { RenderEffect.createBlurEffect(blurRadiusPx, blurRadiusPx, Shader.TileMode.CLAMP).asComposeRenderEffect() }.getOrNull()
        } else null
    }

    val specularRimBrush = remember(palette, effectiveBorderAlphaScale) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to palette.rimHighlight.copy(alpha = (0.68f * effectiveBorderAlphaScale).coerceIn(0f, 0.95f)),
                0.5f to palette.rimCyanTint.copy(alpha = (0.24f * effectiveBorderAlphaScale).coerceIn(0f, 0.70f)),
                1.0f to palette.rimHighlight.copy(alpha = (0.36f * effectiveBorderAlphaScale).coerceIn(0f, 0.85f))
            )
        )
    }

    val lensingSurfaceBrush = remember(effectiveBaseColor, effectiveAlpha, palette) {
        Brush.verticalGradient(
            colors = listOf(
                effectiveBaseColor.copy(alpha = (effectiveAlpha + 0.10f).coerceAtMost(0.92f)),
                effectiveBaseColor.copy(alpha = effectiveAlpha),
                palette.accentSecondary.copy(alpha = (effectiveAlpha * 0.22f).coerceAtMost(0.24f))
            )
        )
    }

    Box(
        propagateMinConstraints = true,
        modifier = modifier
            .clip(shape)
            .background(brush = lensingSurfaceBrush)
            .drawBehind {
                if (effectiveGlowStrength > 0.05f && settings.performanceMode != PerformanceMode.PERFORMANCE) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(effectiveGlowColor.copy(alpha = 0.14f * effectiveGlowStrength), Color.Transparent),
                            center = Offset(size.width * 0.25f, size.height * 0.15f),
                            radius = size.maxDimension * 0.85f
                        ),
                        radius = size.maxDimension * 0.85f,
                        center = Offset(size.width * 0.25f, size.height * 0.15f)
                    )
                }
            }
    ) {
        if (frostComposeEffect != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { renderEffect = frostComposeEffect; alpha = 0.45f }
                    .background(effectiveBaseColor.copy(alpha = effectiveAlpha * 0.6f))
            )
        }

        Box(
            modifier = Modifier
                .drawBehind {
                    val glossHeight = size.height * 0.34f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = if (palette.isLightGlass) 0.30f else 0.20f), Color.Transparent),
                            startY = 0f, endY = glossHeight
                        ),
                        size = size.copy(height = glossHeight)
                    )
                    val bottomStart = size.height * 0.78f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, palette.rimCyanTint.copy(alpha = 0.10f)),
                            startY = bottomStart, endY = size.height
                        ),
                        topLeft = Offset(0f, bottomStart),
                        size = size.copy(height = size.height - bottomStart)
                    )
                }
                .border(width = borderWidth, brush = specularRimBrush, shape = shape),
            content = content
        )
    }
}
