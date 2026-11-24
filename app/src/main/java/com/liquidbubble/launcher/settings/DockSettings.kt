package com.liquidbubble.launcher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.data.model.DockPosition
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette

@Composable
fun DockAndIslandSettingsSection(
    settings: LauncherSettingsState,
    onUpdate: ((LauncherSettingsState) -> LauncherSettingsState) -> Unit
) {
    val palette = LocalLiquidPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Floating Bubble Dock", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                SettingSlider("Dock Height (dp)", settings.dockHeightDp, 68f..106f) { v -> onUpdate { it.copy(dockHeightDp = v) } }
                SettingSlider("Horizontal Margin (dp)", settings.dockHorizontalMarginDp, 8f..36f) { v -> onUpdate { it.copy(dockHorizontalMarginDp = v) } }
                SettingSlider("Dock Icon Size (dp)", settings.dockIconSizeDp, 42f..68f) { v -> onUpdate { it.copy(dockIconSizeDp = v) } }
                SettingSlider("Dock Icon Spacing (dp)", settings.dockIconSpacingDp, 4f..22f) { v -> onUpdate { it.copy(dockIconSpacingDp = v) } }
                SettingSlider("Dock Transparency", settings.dockAlpha, 0.18f..0.85f) { v -> onUpdate { it.copy(dockAlpha = v) } }
                SettingSwitch("Bubble Bounce Magnification", settings.dockMagnificationEnabled) { v -> onUpdate { it.copy(dockMagnificationEnabled = v) } }
                Text("Dock Position", color = palette.textSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DockPosition.entries.forEach { pos ->
                        val selected = settings.dockPosition == pos
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (selected) palette.accentPrimary.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdate { it.copy(dockPosition = pos) } }.padding(vertical = 10.dp)
                        ) { Text(pos.displayName, color = IceWhite, fontSize = 12.sp) }
                    }
                }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dynamic Bubble Island", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                SettingSwitch("Enable Dynamic Island", settings.dynamicIslandEnabled) { v -> onUpdate { it.copy(dynamicIslandEnabled = v) } }
                SettingSlider("Collapsed Width Fraction", settings.islandWidthFraction, 0.46f..0.82f) { v -> onUpdate { it.copy(islandWidthFraction = v) } }
                SettingSlider("Collapsed Height (dp)", settings.islandHeightDp, 34f..46f) { v -> onUpdate { it.copy(islandHeightDp = v) } }
                SettingSwitch("Show Weather in Island", settings.islandWeatherVisible) { v -> onUpdate { it.copy(islandWeatherVisible = v) } }
                SettingSwitch("Show Battery in Island", settings.islandBatteryVisible) { v -> onUpdate { it.copy(islandBatteryVisible = v) } }
                SettingSwitch("Show Now Playing Waveform", settings.islandMusicVisible) { v -> onUpdate { it.copy(islandMusicVisible = v) } }
            }
        }
    }
}
