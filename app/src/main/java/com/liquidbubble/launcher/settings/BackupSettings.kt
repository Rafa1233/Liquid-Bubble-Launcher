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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.BuildConfig
import com.liquidbubble.launcher.RiiLauncherApp
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
    val context = LocalContext.current
    val lastCrash = remember { (context.applicationContext as? RiiLauncherApp)?.readLastCrash() }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih Layar Utama Default", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Aktif saat ini: $currentDefaultLauncherLabel", color = palette.rimCyanTint, fontSize = 13.sp)
                if (!isDefaultLauncher) {
                    Button(onClick = onRequestSetDefaultLauncher, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary), modifier = Modifier.fillMaxWidth()) {
                        Text("Jadikan Rii Launcher Default")
                    }
                }
                OutlinedButton(onClick = onOpenSystemHomeSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Ganti Launcher Default / Keluar", color = IceWhite)
                }
                Text("Untuk kembali ke launcher bawaan, ketuk tombol di atas lalu pilih launcher sebelumnya di Pengaturan Sistem.", color = palette.textSecondary, fontSize = 11.5.sp)
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Backup & Pulihkan (JSON)", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onExportBackup, colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary), modifier = Modifier.weight(1f)) { Text("Ekspor") }
                    OutlinedButton(onClick = onImportBackup, modifier = Modifier.weight(1f)) { Text("Impor", color = IceWhite) }
                }
                OutlinedButton(onClick = onRefreshAppList, modifier = Modifier.fillMaxWidth()) { Text("Segarkan Daftar Aplikasi", color = IceWhite) }
                OutlinedButton(onClick = onResetSettings, modifier = Modifier.fillMaxWidth()) { Text("Reset Semua Pengaturan", color = palette.accentWarm) }
            }
        }
        GlassWidget {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tentang Rii Launcher", color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Rii Launcher · v${BuildConfig.VERSION_NAME} (120Hz Ready)", color = IceWhite, fontSize = 12.5.sp)
                Text("Launcher Android bergaya Liquid Glass iPhone 17 Pro Max. Semua pengaturan tersimpan lokal di perangkat Anda.", color = palette.textSecondary, fontSize = 12.sp)
                if (!lastCrash.isNullOrBlank()) {
                    Text("Log diagnostik terakhir:\n${lastCrash.take(260)}", color = palette.accentWarm, fontSize = 10.5.sp)
                }
            }
        }
    }
}
