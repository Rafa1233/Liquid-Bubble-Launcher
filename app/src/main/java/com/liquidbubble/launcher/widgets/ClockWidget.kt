package com.liquidbubble.launcher.widgets

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.LiquidBubbleSurface
import com.liquidbubble.launcher.data.model.WeatherState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ClockWeatherWidget(
    weather: WeatherState,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalLiquidPalette.current
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            val millisUntilNextMinute = ((60 - now.second) * 1000L - (now.nano / 1_000_000L)).coerceIn(500L, 60_000L)
            delay(millisUntilNextMinute)
        }
    }

    val hourText = remember(now, is24Hour) { now.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH" else "hh", Locale.getDefault())) }
    val minuteText = remember(now) { now.format(DateTimeFormatter.ofPattern("mm", Locale.getDefault())) }
    val amPmText = remember(now, is24Hour) { if (is24Hour) "" else now.format(DateTimeFormatter.ofPattern(" a", Locale.getDefault())) }
    val dateLabel = remember(now) { now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(hourText) }
                withStyle(SpanStyle(fontWeight = FontWeight.Thin, color = IceWhite.copy(alpha = 0.80f))) { append(":") }
                withStyle(SpanStyle(fontWeight = FontWeight.Light)) { append(minuteText) }
                if (amPmText.isNotEmpty()) {
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = palette.rimCyanTint)) { append(amPmText) }
                }
            },
            style = TextStyle(
                color = IceWhite, fontSize = 54.sp, letterSpacing = (-1.5).sp,
                shadow = Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 4f), 14f)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            LiquidBubbleSurface(shape = RoundedCornerShape(999.dp), glassAlphaOverride = 0.34f, borderWidth = 1.dp, enableGlow = false) {
                Text(dateLabel, color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
            LiquidBubbleSurface(
                shape = RoundedCornerShape(999.dp), glassAlphaOverride = 0.36f, borderWidth = 1.dp,
                modifier = Modifier.clickable(onClick = onWeatherClick)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    SpatialWeatherCanvasIcon(weatherCode = weather.weatherCode, modifier = Modifier.size(18.dp))
                    val tempDisplay = weather.temperatureCelsius?.let { "${it.roundToInt()}°C" } ?: "--°C"
                    Text("$tempDisplay · ${weather.locationName}", color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
