package com.liquidbubble.launcher.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.LiquidAppIcon
import com.liquidbubble.launcher.components.LiquidBubbleSurface
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.LauncherThemePreset
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
import com.liquidbubble.launcher.wallpaper.BuiltInWallpapers

@Composable
fun OnboardingScreen(
    installedApps: List<AppInfo>,
    appManager: AppManager,
    isDefaultLauncher: Boolean,
    favoriteKeys: List<String>,
    onSetDefaultLauncher: () -> Unit,
    onSelectThemePreset: (LauncherThemePreset) -> Unit,
    onSelectBuiltInWallpaper: (Int) -> Unit,
    onUseAuraWallpaper: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onFinish: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    val settings = LocalLiquidSettings.current
    var step by remember { mutableIntStateOf(0) }
    var showLearnMore by remember { mutableStateOf(false) }
    val totalSteps = 8

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        LiquidBubbleSurface(
            shape = RoundedCornerShape(34.dp),
            glassAlphaOverride = 0.68f,
            modifier = Modifier.fillMaxSize().align(Alignment.Center)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize().padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Step ${step + 1} of $totalSteps", color = palette.rimCyanTint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onFinish) { Text("Skip", color = palette.textSecondary) }
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (step) {
                        0 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(palette.accentPrimary, palette.accentSecondary)))
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = IceWhite, modifier = Modifier.size(38.dp))
                            }
                            Text("Liquid Bubble Launcher", color = IceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("An Android Home Screen crafted with Spatial Liquid Glass, Titanium Mesh Aura, interactive Dynamic Island, and full Android customization.", color = palette.textSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                        1 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Choose Your Glass Theme", color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            LiquidBubbleSurface(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(92.dp)) {
                                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("6-Layer Refractive Glass", color = IceWhite, fontWeight = FontWeight.SemiBold)
                                    Text("Specular rim, inner top gloss, depth shadow & blur", color = palette.textSecondary, fontSize = 12.sp)
                                }
                            }
                            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(170.dp)) {
                                items(LauncherThemePreset.entries.filter { it != LauncherThemePreset.CUSTOM }) { preset ->
                                    val selected = settings.themePreset == preset
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (selected) palette.accentPrimary.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f))
                                            .border(1.dp, if (selected) IceWhite else Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                                            .clickable { onSelectThemePreset(preset) }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                    ) {
                                        Text(preset.displayName, color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        2 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Select Spatial Wallpaper", color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(palette.accentPrimary.copy(alpha = 0.35f)).clickable(onClick = onUseAuraWallpaper).padding(14.dp)
                            ) {
                                Text("Titanium Mesh Aura (Live Orbs)", color = IceWhite, fontWeight = FontWeight.SemiBold)
                            }
                            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(190.dp)) {
                                items(BuiltInWallpapers) { wp ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.height(54.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(wp.topColor, wp.midColor, wp.glowColor))).clickable { onSelectBuiltInWallpaper(wp.id) }
                                    ) {
                                        Text(wp.name, color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        3 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.Home, contentDescription = null, tint = palette.rimCyanTint, modifier = Modifier.size(48.dp))
                            Text("Set as Default Launcher", color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                            Text(
                                text = if (isDefaultLauncher) "Liquid Bubble is currently active as your Default Home App." else "Make Liquid Bubble your Home screen so pressing the Home gesture or button always returns here.",
                                color = palette.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
                            )
                            Button(onClick = onSetDefaultLauncher, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary), modifier = Modifier.fillMaxWidth()) {
                                Text(if (isDefaultLauncher) "Default Launcher Active" else "Set as Default Launcher")
                            }
                            OutlinedButton(onClick = { step++ }, modifier = Modifier.fillMaxWidth()) {
                                Text("Continue Without Setting Default", color = IceWhite)
                            }
                            TextButton(onClick = { showLearnMore = !showLearnMore }) { Text("Learn More", color = palette.rimCyanTint) }
                            if (showLearnMore) {
                                Text("You can switch back to your previous launcher anytime from Liquid Settings → System & Backup, or uninstall normally in Android Settings.", color = palette.textSecondary, fontSize = 11.5.sp, textAlign = TextAlign.Center)
                            }
                        }
                        4 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = palette.rimCyanTint, modifier = Modifier.size(44.dp))
                            Text("Privacy-First Permissions", color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Text("We never ask for unnecessary permissions at startup. Optional permissions are requested only if you use a feature:", color = palette.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.06f)).padding(14.dp)) {
                                Text("• Camera: Only when toggling Flashlight", color = IceWhite, fontSize = 12.5.sp)
                                Text("• Location: Optional for local Open-Meteo weather", color = IceWhite, fontSize = 12.5.sp)
                                Text("• Notification Access: Optional for Now Playing & Badges", color = IceWhite, fontSize = 12.5.sp)
                            }
                        }
                        5 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Pick Favorite Home Apps", color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Text("Selected: ${favoriteKeys.size} apps (tap to toggle)", color = palette.textSecondary, fontSize = 12.sp)
                            LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(250.dp)) {
                                items(installedApps.take(24), key = { it.componentKey }) { app ->
                                    val selected = app.componentKey in favoriteKeys
                                    Box(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) palette.accentPrimary.copy(alpha = 0.32f) else Color.Transparent).padding(4.dp)) {
                                        LiquidAppIcon(app = app, appManager = appManager, iconSize = 48.dp, onClick = { onToggleFavorite(app.componentKey) })
                                    }
                                }
                            }
                        }
                        6 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Floating Bubble Dock", color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Text("Your Phone, Messages, Browser, and Camera shortcuts were automatically mapped via system intents. Long-press any Dock icon anytime to replace or drag-reorder.", color = palette.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                            LiquidBubbleSurface(shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth().height(78.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                                    settings.dockAppKeys.take(4).forEach { key ->
                                        val app = installedApps.firstOrNull { it.componentKey == key }
                                        if (app != null) LiquidAppIcon(app = app, appManager = appManager, iconSize = 48.dp, showLabel = false, onClick = {})
                                    }
                                }
                            }
                        }
                        7 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(56.dp))
                            Text("Ready for Liquid Glass", color = IceWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Swipe up for App Library, tap Dynamic Island for controls & music, or long-press the wallpaper anytime to customize.", color = palette.textSecondary, fontSize = 13.5.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (step > 0) TextButton(onClick = { step-- }) { Text("Back", color = palette.textSecondary) } else Spacer(modifier = Modifier.size(64.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(totalSteps) { idx ->
                            Box(modifier = Modifier.size(if (idx == step) 10.dp else 6.dp).clip(CircleShape).background(if (idx == step) IceWhite else Color.White.copy(alpha = 0.28f)))
                        }
                    }
                    Button(onClick = { if (step < totalSteps - 1) step++ else onFinish() }, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary)) {
                        Text(if (step < totalSteps - 1) "Next" else "Enter Home")
                    }
                }
            }
        }
    }
}
