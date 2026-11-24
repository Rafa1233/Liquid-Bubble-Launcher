package com.liquidbubble.launcher.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.data.model.BatteryState
import com.liquidbubble.launcher.data.model.SystemMetricsState
import com.liquidbubble.launcher.ui.theme.CyberViolet
import com.liquidbubble.launcher.ui.theme.ElectricCobalt
import com.liquidbubble.launcher.ui.theme.SunsetCoral
import kotlin.math.roundToInt

@Composable
fun SystemMonitorWidget(battery: BatteryState, metrics: SystemMetricsState, modifier: Modifier = Modifier) {
    val batteryPct = battery.levelPercent.coerceAtLeast(0)
    val ramPct = (metrics.usedRamFraction * 100f).roundToInt()
    val storagePct = (metrics.usedStorageFraction * 100f).roundToInt()
    val usedGb = ((metrics.totalStorageBytes - metrics.availableStorageBytes) / (1024f * 1024f * 1024f))
    val totalGb = (metrics.totalStorageBytes / (1024f * 1024f * 1024f))

    GlassWidget(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            GlassRingGauge(
                progress = batteryPct / 100f,
                centerLabel = if (battery.levelPercent >= 0) "$batteryPct%" else "--",
                subtitle = if (battery.isCharging) "Charging" else "Battery",
                gradientColors = if (battery.isCharging) listOf(Color(0xFF4ADE80), Color(0xFF2DD4BF), Color(0xFF4ADE80))
                else listOf(ElectricCobalt, Color(0xFF60A5FA), ElectricCobalt),
                badgeIcon = if (battery.isCharging) {
                    { Icon(Icons.Rounded.Bolt, contentDescription = "Charging", tint = Color(0xFF4ADE80), modifier = Modifier.size(13.dp)) }
                } else null
            )
            GlassRingGauge(
                progress = metrics.usedRamFraction,
                centerLabel = if (metrics.totalRamBytes > 0L) "$ramPct%" else "--",
                subtitle = "System RAM",
                gradientColors = listOf(CyberViolet, Color(0xFFC084FC), CyberViolet)
            )
            GlassRingGauge(
                progress = metrics.usedStorageFraction,
                centerLabel = if (metrics.totalStorageBytes > 0L) "$storagePct%" else "--",
                subtitle = if (totalGb > 0f) "${usedGb.roundToInt()}/${totalGb.roundToInt()} GB" else "Storage",
                gradientColors = listOf(SunsetCoral, Color(0xFFFB923C), SunsetCoral)
            )
        }
    }
}
