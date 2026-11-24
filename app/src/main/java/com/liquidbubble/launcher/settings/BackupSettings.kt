package com.liquidbubble.launcher.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.BuildConfig
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette

@Composable
fun SystemAndBackupSettingsSection(
    isDefaultLauncher: Boolean,
    currentDefaultLauncherLabel: String,
    onRequestSetDefaultLauncher: () -> Unit,
    onOpenSystemHomeSettings: () -> Unit,
    onRefreshAppList: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onResetSettings: () -> Unit
) {
    val palette = LocalLiquidPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Default Home App", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Current Default: $currentDefaultLauncherLabel", color = palette.rimCyanTint, fontSize = 13.sp)
                if (!isDefaultLauncher) {
                    Button(onClick = onRequestSetDefaultLauncher, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary), modifier = Modifier.fillMaxWidth()) {
                        Text("Set as Default Launcher")
                    }
                }
                OutlinedButton(onClick = onOpenSystemHomeSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Switch Default Launcher / Exit", color = IceWhite)
                }
                Text("To return to your previous launcher, tap Switch Default Launcher above and choose your previous Home app in Android System Settings.", color = palette.textSecondary, fontSize = 11.5.sp)
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Backup & Restore (SAF JSON)", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Export or restore theme, glass optics, dock layout, folders, favorites, and gesture preferences.", color = palette.textSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onExportBackup, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary), modifier = Modifier.weight(1f)) { Text("Export Backup") }
                    OutlinedButton(onClick = onImportBackup, modifier = Modifier.weight(1f)) { Text("Import Backup", color = IceWhite) }
                }
                OutlinedButton(onClick = onRefreshAppList, modifier = Modifier.fillMaxWidth()) { Text("Refresh Installed App List", color = IceWhite) }
                OutlinedButton(onClick = onResetSettings, modifier = Modifier.fillMaxWidth()) { Text("Reset All Launcher Preferences", color = palette.accentWarm) }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("About & Privacy", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Liquid Bubble Launcher · v${BuildConfig.VERSION_NAME}", color = IceWhite, fontSize = 12.5.sp)
                Text("This is an independent Android Home Launcher inspired by Liquid Glass spatial aesthetics. All launcher settings stay locally on your device.", color = palette.textSecondary, fontSize = 12.sp)
            }
        }
    }
}
