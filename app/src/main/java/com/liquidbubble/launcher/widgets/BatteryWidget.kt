package com.liquidbubble.launcher.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.data.model.BatteryState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette

@Composable
fun GlassRingGauge(
    progress: Float,
    centerLabel: String,
    subtitle: String,
    gradientColors: List<Color>,
    ringSize: Dp = 74.dp,
    badgeIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val animatedProgress by animateFloatAsState(progress.coerceIn(0f, 1f), tween(650), label = "ringProgress")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 7.dp.toPx()
                drawArc(Color.White.copy(alpha = 0.12f), -90f, 360f, false, style = Stroke(strokePx, cap = StrokeCap.Round))
                drawArc(Brush.sweepGradient(gradientColors), -90f, 360f * animatedProgress, false, alpha = 0.28f, style = Stroke(strokePx * 1.9f, cap = StrokeCap.Round))
                drawArc(Brush.sweepGradient(gradientColors), -90f, 360f * animatedProgress, false, style = Stroke(strokePx, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (badgeIcon != null) badgeIcon()
                Text(centerLabel, color = IceWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(subtitle, color = palette.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BatteryWidget(battery: BatteryState, modifier: Modifier = Modifier) {
    val palette = LocalLiquidPalette.current
    val pct = battery.levelPercent.coerceAtLeast(0)
    val ringColors = if (battery.isCharging) listOf(Color(0xFF4ADE80), Color(0xFF2DD4BF), Color(0xFF4ADE80))
    else listOf(palette.accentPrimary, palette.accentSecondary, palette.accentPrimary)

    GlassWidget(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Device Power", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (battery.isCharging) "Charging (${battery.pluggedSource})" else "Discharging", color = if (battery.isCharging) Color(0xFF4ADE80) else palette.textSecondary, fontSize = 12.sp)
                if (battery.temperatureCelsius > 0f) Text("Temp ${battery.temperatureCelsius}°C", color = palette.textSecondary.copy(alpha = 0.72f), fontSize = 11.sp)
            }
            GlassRingGauge(
                progress = pct / 100f,
                centerLabel = if (battery.levelPercent >= 0) "$pct%" else "--",
                subtitle = "Battery",
                gradientColors = ringColors,
                badgeIcon = if (battery.isCharging) {
                    { Icon(Icons.Rounded.Bolt, contentDescription = "Charging", tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp)) }
                } else null
            )
        }
    }
}
