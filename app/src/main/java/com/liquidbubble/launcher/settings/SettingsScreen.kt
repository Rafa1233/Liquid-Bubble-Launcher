package com.liquidbubble.launcher.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquidbubble.launcher.BuildConfig
import com.liquidbubble.launcher.components.LiquidBubbleSurface
import com.liquidbubble.launcher.data.model.IconPackInfo
import com.liquidbubble.launcher.launcher.LauncherViewModel
import com.liquidbubble.launcher.ui.theme.ElectricCobalt
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.SunsetCoral
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    initialTabIndex: Int = 0,
    isDefaultLauncher: Boolean,
    currentDefaultLauncherLabel: String,
    iconPacks: List<IconPackInfo>,
    onPickCustomWallpaper: () -> Unit,
    onRequestSetDefaultLauncher: () -> Unit,
    onOpenSystemHomeSettings: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = LocalLiquidPalette.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedSection by remember(initialTabIndex) {
        mutableIntStateOf(if (initialTabIndex == 2) 2 else -1)
    }

    val sectionTitle = when (selectedSection) {
        0 -> "Tampilan & 120Hz"
        1 -> "Ikon & Home Screen"
        2 -> "Wallpaper & Aura"
        3 -> "Control Center & Island"
        4 -> "Umum & Backup"
        else -> "Pengaturan"
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = viewModel.preferencesRepo.exportBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                            ?: error("Cannot open destination stream")
                    }
                }.onSuccess { viewModel.showMessage("Backup Rii Launcher diekspor") }
                    .onFailure { viewModel.showMessage("Gagal mengekspor backup") }
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val raw = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                            ?: error("Cannot read backup file")
                    }
                    viewModel.preferencesRepo.importBackupJson(raw).getOrThrow()
                }.onSuccess { viewModel.showMessage("Backup Rii Launcher dipulihkan") }
                    .onFailure { viewModel.showMessage("File backup tidak valid") }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060810))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { if (selectedSection >= 0) selectedSection = -1 else onClose() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = IceWhite)
                }
                Text(sectionTitle, color = IceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)
            ) {
                when (selectedSection) {
                    -1 -> {
                        LiquidBubbleSurface(shape = RoundedCornerShape(24.dp), glassAlphaOverride = 0.45f) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().clickable { selectedSection = 0 }.padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                    Text("Rii Launcher (${BuildConfig.VERSION_NAME})", color = IceWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Text("Sesuaikan Liquid Glass iPhone 17 Pro Max, 120Hz, Dock, dan Dynamic Island dengan sempurna.", color = palette.textSecondary, fontSize = 12.5.sp)
                                    Text("Jelajahi  ▶", color = palette.rimCyanTint, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.size(12.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(62.dp).clip(CircleShape).background(Brush.linearGradient(listOf(ElectricCobalt, palette.accentSecondary)))
                                ) { Text("Rii", color = IceWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold) }
                            }
                        }

                        LiquidBubbleSurface(shape = RoundedCornerShape(24.dp), glassAlphaOverride = 0.36f) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SettingsHubRow(Icons.Rounded.Tune, Color(0xFF3B82F6), "Control Center & Dynamic Island", "Media, Torch, Wi-Fi, Kecerahan & Dock") { selectedSection = 3 }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                SettingsHubRow(Icons.Rounded.Palette, Color(0xFF8B5CF6), "Tampilan & Liquid Glass 120Hz", "Tema iOS 26, Blur, & Mode Performa") { selectedSection = 0 }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                SettingsHubRow(Icons.Rounded.Wallpaper, SunsetCoral, "Wallpaper & Titanium Aura", "Live Aura Orbs, Preset & Foto Galeri") { selectedSection = 2 }
                            }
                        }

                        Text("Umum", color = palette.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
                        LiquidBubbleSurface(shape = RoundedCornerShape(24.dp), glassAlphaOverride = 0.36f) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SettingsHubRow(Icons.Rounded.Apps, Color(0xFF06B6D4), "Ikon, Grid & Gestur", "Squircle, Icon Pack, Lencana Notifikasi") { selectedSection = 1 }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                SettingsHubRow(
                                    Icons.Rounded.Home, Color(0xFF10B981), "Pilih Layar Utama Default",
                                    if (isDefaultLauncher) "Aktif: Rii Launcher" else "Saat ini: $currentDefaultLauncherLabel",
                                    onClick = onRequestSetDefaultLauncher
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                SettingsHubRow(Icons.Rounded.AutoAwesome, Color(0xFFF59E0B), "Backup, Pulihkan & Keluar Launcher", "Simpan konfigurasi JSON & Tentang") { selectedSection = 4 }
                            }
                        }
                    }
                    0 -> AppearanceSettingsSection(settings = settings, onUpdate = { viewModel.updateSettings(it) })
                    1 -> IconAndHomeSettingsSection(settings = settings, iconPacks = iconPacks, onApplyIconPack = { viewModel.applyIconPack(it) }, onUpdate = { viewModel.updateSettings(it) })
                    2 -> WallpaperSettingsSection(settings = settings, onPickCustomWallpaper = onPickCustomWallpaper, onUpdate = { viewModel.updateSettings(it) })
                    3 -> DockAndIslandSettingsSection(settings = settings, onUpdate = { viewModel.updateSettings(it) })
                    4 -> SystemAndBackupSettingsSection(
                        isDefaultLauncher = isDefaultLauncher,
                        currentDefaultLauncherLabel = currentDefaultLauncherLabel,
                        onRequestSetDefaultLauncher = onRequestSetDefaultLauncher,
                        onOpenSystemHomeSettings = onOpenSystemHomeSettings,
                        onRefreshAppList = { viewModel.refreshInstalledApps(); viewModel.showMessage("Daftar aplikasi diperbarui") },
                        onExportBackup = { exportBackupLauncher.launch("rii_launcher_backup.json") },
                        onImportBackup = { importBackupLauncher.launch(arrayOf("application/json", "text/plain")) },
                        onResetSettings = {
                            scope.launch {
                                viewModel.preferencesRepo.resetAllSettings()
                                viewModel.showMessage("Semua pengaturan direset")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHubRow(icon: ImageVector, badgeColor: Color, title: String, subtitle: String, onClick: () -> Unit) {
    val palette = LocalLiquidPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(badgeColor)) {
            Icon(icon, contentDescription = null, tint = IceWhite, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = IceWhite, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = palette.textSecondary, fontSize = 11.5.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = palette.textSecondary)
    }
}
