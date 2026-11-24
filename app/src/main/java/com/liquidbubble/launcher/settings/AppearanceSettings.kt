package com.liquidbubble.launcher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.LauncherThemePreset
import com.liquidbubble.launcher.data.model.PerformanceMode
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import java.util.Locale

@Composable
fun AppearanceSettingsSection(
    settings: LauncherSettingsState,
    onUpdate: ((LauncherSettingsState) -> LauncherSettingsState) -> Unit
) {
    val palette = LocalLiquidPalette.current
    val accentSwatches = listOf(0xFF2563EB, 0xFF7C3AED, 0xFFF43F5E, 0xFF06B6D4, 0xFF10B981, 0xFFF59E0B)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Liquid Theme Preset", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                LauncherThemePreset.entries.chunked(2).forEach { rowPresets ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowPresets.forEach { preset ->
                            val selected = settings.themePreset == preset
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) palette.accentPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, if (selected) IceWhite else Color.Transparent, RoundedCornerShape(14.dp))
                                    .clickable { onUpdate { it.copy(themePreset = preset) } }
                                    .padding(vertical = 10.dp, horizontal = 6.dp)
                            ) { Text(preset.displayName, color = IceWhite, fontSize = 12.sp) }
                        }
                    }
                }
                Text("Custom Accent Color", color = palette.textSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    accentSwatches.forEach { hex ->
                        val selected = settings.themePreset == LauncherThemePreset.CUSTOM && settings.customAccentHex == hex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(2.dp, if (selected) IceWhite else Color.Transparent, CircleShape)
                                .clickable { onUpdate { it.copy(themePreset = LauncherThemePreset.CUSTOM, customAccentHex = hex) } }
                        )
                    }
                }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Glass Surface Optics", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                SettingSlider("Glass Transparency", settings.glassAlpha, 0.16f..0.82f) { v -> onUpdate { it.copy(glassAlpha = v) } }
                SettingSlider("Glass Blur Radius", settings.glassBlurDp, 0f..24f) { v -> onUpdate { it.copy(glassBlurDp = v) } }
                SettingSlider("Specular Border Intensity", settings.borderIntensity, 0.2f..1.2f) { v -> onUpdate { it.copy(borderIntensity = v) } }
                SettingSlider("Ambient Glow Strength", settings.glowStrength, 0f..1f) { v -> onUpdate { it.copy(glowStrength = v) } }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Adaptive Performance & Motion", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                PerformanceMode.entries.forEach { mode ->
                    val selected = settings.performanceMode == mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) palette.accentPrimary.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.06f))
                            .clickable { onUpdate { it.copy(performanceMode = mode) } }
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode.displayName, color = IceWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(mode.subtitle, color = palette.textSecondary, fontSize = 11.sp)
                        }
                    }
                }
                SettingSwitch("Bubble Bounce Physics", settings.bubblePhysicsEnabled) { v -> onUpdate { it.copy(bubblePhysicsEnabled = v) } }
                SettingSwitch("Folder Morph Animation", settings.folderAnimationEnabled) { v -> onUpdate { it.copy(folderAnimationEnabled = v) } }
                SettingSwitch("Battery Saver (Minimal Motion)", settings.batterySaverMode) { v -> onUpdate { it.copy(batterySaverMode = v) } }
                SettingSwitch("Reduced Motion (Accessibility)", settings.reducedMotion) { v -> onUpdate { it.copy(reducedMotion = v) } }
                SettingSwitch("Haptic Feedback", settings.hapticsEnabled) { v -> onUpdate { it.copy(hapticsEnabled = v) } }
            }
        }
    }
}

@Composable
internal fun SettingSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    val palette = LocalLiquidPalette.current
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = IceWhite, fontSize = 13.sp)
            Text(String.format(Locale.US, "%.2f", value), color = palette.rimCyanTint, fontSize = 12.sp)
        }
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
internal fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = IceWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
