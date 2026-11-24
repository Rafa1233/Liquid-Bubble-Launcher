package com.liquidbubble.launcher.widgets

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.data.model.WeatherState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherWidget(
    weather: WeatherState,
    onChangeLocationClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    GlassWidget(modifier = modifier.clickable(onClick = onRefreshClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(weather.locationName, color = palette.rimCyanTint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onChangeLocationClick))
                Spacer(modifier = Modifier.height(2.dp))
                Text(weather.temperatureCelsius?.let { "${it.roundToInt()}°" } ?: "--°", color = IceWhite, fontSize = 36.sp, fontWeight = FontWeight.Light)
                Text(weather.errorMessage ?: weather.conditionLabel, color = palette.textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                val details = buildList {
                    weather.humidityPercent?.let { add("Humidity $it%") }
                    weather.windSpeedKmh?.let { add("Wind ${it.roundToInt()} km/h") }
                    weather.lastUpdatedEpochMs?.let { ts -> add("Updated ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))}") }
                }.joinToString(" · ")
                if (details.isNotEmpty()) Text(details, color = palette.textSecondary.copy(alpha = 0.72f), fontSize = 11.sp)
            }
            SpatialWeatherCanvasIcon(weatherCode = weather.weatherCode, modifier = Modifier.size(64.dp))
        }
    }
}

@Composable
fun SpatialWeatherCanvasIcon(weatherCode: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "weatherFloat")
    val drift by transition.animateFloat(-1.5f, 1.5f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "cloudDrift")

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val isSunny = weatherCode in listOf(-1, 0, 1)
        val hasSun = weatherCode in listOf(-1, 0, 1, 2)
        val hasCloud = weatherCode !in listOf(0)
        val isRain = weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)

        if (hasSun) {
            val sunCenter = if (isSunny) Offset(w * 0.5f, h * 0.5f) else Offset(w * 0.36f, h * 0.36f)
            val sunRadius = if (isSunny) w * 0.30f else w * 0.22f
            drawCircle(Brush.radialGradient(listOf(Color(0x66FBBF24), Color.Transparent), sunCenter, sunRadius * 1.9f), sunRadius * 1.9f, sunCenter)
            drawCircle(Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFF97316))), sunRadius, sunCenter)
        }
        if (hasCloud) {
            val offsetDp = drift.dp.toPx() * 0.5f
            val cloudBrush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.95f), Color(0xFFCBD5E1).copy(alpha = 0.85f)))
            drawRoundRect(cloudBrush, Offset(w * 0.16f + offsetDp, h * 0.44f), Size(w * 0.68f, h * 0.30f), CornerRadius(h * 0.15f, h * 0.15f))
            drawCircle(cloudBrush, w * 0.19f, Offset(w * 0.40f + offsetDp, h * 0.44f))
            drawCircle(cloudBrush, w * 0.14f, Offset(w * 0.60f + offsetDp, h * 0.47f))
        }
        if (isRain) {
            listOf(0.30f, 0.50f, 0.70f).forEach { rx ->
                drawLine(Color(0xFF60A5FA), Offset(w * rx, h * 0.78f), Offset(w * (rx - 0.05f), h * 0.92f), strokeWidth = w * 0.055f)
            }
        }
    }
}
