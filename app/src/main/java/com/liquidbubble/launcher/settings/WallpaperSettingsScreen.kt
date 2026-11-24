package com.liquidbubble.launcher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.WallpaperMode
import com.liquidbubble.launcher.data.model.WallpaperScaleMode
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.wallpaper.BuiltInWallpapers

@Composable
fun WallpaperSettingsSection(
    settings: LauncherSettingsState,
    onPickCustomWallpaper: () -> Unit,
    onUpdate: ((LauncherSettingsState) -> LauncherSettingsState) -> Unit
) {
    val palette = LocalLiquidPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Wallpaper Source", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    WallpaperMode.entries.forEach { mode ->
                        val selected = settings.wallpaperMode == mode
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(if (selected) palette.accentPrimary.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdate { it.copy(wallpaperMode = mode) } }.padding(vertical = 10.dp, horizontal = 4.dp)
                        ) { Text(mode.displayName, color = IceWhite, fontSize = 11.sp) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onPickCustomWallpaper, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary), modifier = Modifier.weight(1f)) {
                        Text("Choose Photo")
                    }
                    OutlinedButton(
                        onClick = {
                            onUpdate { it.copy(wallpaperMode = WallpaperMode.TITANIUM_AURA, customWallpaperUri = null, wallpaperBlurDp = 0f, wallpaperDimAlpha = 0.12f) }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Reset Aura", color = IceWhite) }
                }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Built-In Spatial Presets", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                BuiltInWallpapers.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { preset ->
                            val selected = settings.wallpaperMode == WallpaperMode.BUILT_IN_PRESET && settings.builtInWallpaperIndex == preset.id
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(preset.topColor, preset.midColor, preset.glowColor)))
                                    .border(1.5.dp, if (selected) IceWhite else Color.Transparent, RoundedCornerShape(16.dp))
                                    .clickable { onUpdate { it.copy(wallpaperMode = WallpaperMode.BUILT_IN_PRESET, builtInWallpaperIndex = preset.id) } }
                            ) { Text(preset.name, color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        }
                    }
                }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Titanium Aura & Image Adjustments", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                SettingSwitch("Aura Motion & Sensor Parallax", settings.auraMotionEnabled) { v -> onUpdate { it.copy(auraMotionEnabled = v) } }
                SettingSlider("Aura Animation Speed", settings.auraAnimationSpeed, 0.25f..2.2f) { v -> onUpdate { it.copy(auraAnimationSpeed = v) } }
                SettingSlider("Aura Orb Size", settings.auraOrbSize, 0.6f..1.5f) { v -> onUpdate { it.copy(auraOrbSize = v) } }
                SettingSlider("Aura Glow Strength", settings.auraGlowStrength, 0.2f..1.3f) { v -> onUpdate { it.copy(auraGlowStrength = v) } }
                SettingSlider("Custom Photo Blur (API 31+)", settings.wallpaperBlurDp, 0f..25f) { v -> onUpdate { it.copy(wallpaperBlurDp = v) } }
                SettingSlider("Wallpaper Dim Overlay", settings.wallpaperDimAlpha, 0f..0.7f) { v -> onUpdate { it.copy(wallpaperDimAlpha = v) } }
                Text("Custom Image Scaling", color = palette.textSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    WallpaperScaleMode.entries.forEach { mode ->
                        val selected = settings.wallpaperScaleMode == mode
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (selected) palette.accentPrimary.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdate { it.copy(wallpaperScaleMode = mode) } }.padding(vertical = 10.dp)
                        ) { Text(mode.displayName, color = IceWhite, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}
