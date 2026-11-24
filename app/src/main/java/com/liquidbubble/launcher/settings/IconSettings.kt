package com.liquidbubble.launcher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.liquidbubble.launcher.data.model.IconPackInfo
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import kotlin.math.roundToInt

@Composable
fun IconAndHomeSettingsSection(
    settings: LauncherSettingsState,
    iconPacks: List<IconPackInfo>,
    onApplyIconPack: (String?) -> Unit,
    onUpdate: ((LauncherSettingsState) -> LauncherSettingsState) -> Unit
) {
    val palette = LocalLiquidPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("iOS-Style Squircle Icons", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                SettingSlider("Icon Size (dp)", settings.iconSizeDp, 46f..74f) { v -> onUpdate { it.copy(iconSizeDp = v) } }
                SettingSlider("Squircle Radius (dp)", settings.iconCornerRadiusDp, 8f..28f) { v -> onUpdate { it.copy(iconCornerRadiusDp = v) } }
                SettingSlider("Label Font Size (sp)", settings.labelFontSizeSp, 9f..15f) { v -> onUpdate { it.copy(labelFontSizeSp = v) } }
                SettingSwitch("Show App Labels", settings.showAppLabels) { v -> onUpdate { it.copy(showAppLabels = v) } }
                SettingSwitch("Apply Specular Glass Sheen", settings.applyIconMask) { v -> onUpdate { it.copy(applyIconMask = v) } }
                SettingSwitch("Notification Badges", settings.notificationBadgesEnabled) { v -> onUpdate { it.copy(notificationBadgesEnabled = v) } }
                SettingSwitch("Show Badge Count Numbers", settings.showBadgeCount) { v -> onUpdate { it.copy(showBadgeCount = v) } }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Installed Icon Packs", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (settings.activeIconPackPackage == null) palette.accentPrimary.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.06f))
                        .clickable { onApplyIconPack(null) }.padding(12.dp)
                ) { Text("System Default Icons", color = IceWhite, fontSize = 13.sp) }
                if (iconPacks.isEmpty()) {
                    Text("No compatible icon packs detected on device", color = palette.textSecondary, fontSize = 12.sp)
                } else {
                    iconPacks.forEach { pack ->
                        val selected = settings.activeIconPackPackage == pack.packageName
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (selected) palette.accentPrimary.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.06f))
                                .clickable { onApplyIconPack(pack.packageName) }.padding(12.dp)
                        ) { Text(pack.label, color = IceWhite, fontSize = 13.sp) }
                    }
                }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Home Grid & Gestures", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                SettingSlider("Home Columns (${settings.homeColumns})", settings.homeColumns.toFloat(), 3f..6f) { v -> onUpdate { it.copy(homeColumns = v.roundToInt()) } }
                SettingSlider("Drawer Columns (${settings.drawerColumns})", settings.drawerColumns.toFloat(), 3f..6f) { v -> onUpdate { it.copy(drawerColumns = v.roundToInt()) } }
                SettingSlider("Home Page Count (${settings.pageCount})", settings.pageCount.toFloat(), 3f..6f) { v -> onUpdate { it.copy(pageCount = v.roundToInt()) } }
                SettingSwitch("Swipe Up for App Library", settings.swipeUpForDrawer) { v -> onUpdate { it.copy(swipeUpForDrawer = v) } }
                SettingSwitch("Swipe Down for Search", settings.swipeDownForSearch) { v -> onUpdate { it.copy(swipeDownForSearch = v) } }
                SettingSwitch("Long-Press Wallpaper for Edit Mode", settings.longPressWallpaperForEdit) { v -> onUpdate { it.copy(longPressWallpaperForEdit = v) } }
                SettingSwitch("Double-Tap Wallpaper Toggles Island", settings.doubleTapLockOrQuickAction) { v -> onUpdate { it.copy(doubleTapLockOrQuickAction = v) } }
            }
        }
    }
}
