package com.liquidbubble.launcher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

    val effectiveAlpha = (glassAlphaOverride ?: settings.glassAlpha).coerceIn(0.14f, 0.88f)
    val effectiveBaseColor = tintColorOverride ?: palette.glassBase
    val effectiveGlowColor = glowColorOverride ?: palette.accentPrimary
    val effectiveBorderAlphaScale = settings.borderIntensity.coerceIn(0.2f, 1.2f)
    val effectiveGlowStrength = if (enableGlow && settings.performanceMode != PerformanceMode.PERFORMANCE) settings.glowStrength.coerceIn(0f, 1f) else 0f

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
            .drawWithCache {
                val glossHeight = size.height * 0.34f
                val bottomStart = size.height * 0.78f
                val glowRadius = size.maxDimension * 0.85f
                val glowCenter = Offset(size.width * 0.25f, size.height * 0.15f)
                val glowBrush = if (effectiveGlowStrength > 0.05f) {
                    Brush.radialGradient(listOf(effectiveGlowColor.copy(alpha = 0.14f * effectiveGlowStrength), Color.Transparent), glowCenter, glowRadius)
                } else null
                val glossBrush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = if (palette.isLightGlass) 0.30f else 0.20f), Color.Transparent),
                    startY = 0f, endY = glossHeight
                )
                val bottomBrush = Brush.verticalGradient(
                    listOf(Color.Transparent, palette.rimCyanTint.copy(alpha = 0.10f)),
                    startY = bottomStart, endY = size.height
                )

                onDrawBehind {
                    if (glowBrush != null) drawCircle(glowBrush, glowRadius, glowCenter)
                    drawRect(glossBrush, size = size.copy(height = glossHeight))
                    drawRect(bottomBrush, topLeft = Offset(0f, bottomStart), size = size.copy(height = size.height - bottomStart))
                }
            }
            .border(width = borderWidth, brush = specularRimBrush, shape = shape),
        content = content
    )
}
