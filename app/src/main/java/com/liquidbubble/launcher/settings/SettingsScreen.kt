package com.liquidbubble.launcher.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquidbubble.launcher.data.model.IconPackInfo
import com.liquidbubble.launcher.launcher.LauncherViewModel
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
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

    val tabs = listOf("Appearance", "Icons & Home", "Wallpaper", "Dock & Island", "System & Backup")
    var selectedTab by remember(initialTabIndex) { mutableIntStateOf(initialTabIndex.coerceIn(0, tabs.lastIndex)) }

    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = viewModel.preferencesRepo.exportBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                            ?: error("Cannot open destination stream")
                    }
                }.onSuccess { viewModel.showMessage("Launcher backup exported") }
                    .onFailure { viewModel.showMessage("Failed to export backup") }
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
                }.onSuccess { viewModel.showMessage("Launcher backup restored") }
                    .onFailure { viewModel.showMessage("Invalid backup file") }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.backgroundBase.copy(alpha = 0.94f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Home", tint = IceWhite) }
                Text("Liquid Settings", color = IceWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                tabs.forEachIndexed { idx, label ->
                    val active = selectedTab == idx
                    Box(
                        modifier = Modifier.clip(CircleShape)
                            .background(if (active) palette.accentPrimary.copy(alpha = 0.56f) else Color.White.copy(alpha = 0.10f))
                            .clickable { selectedTab = idx }.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) { Text(label, color = IceWhite, fontSize = 12.5.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal) }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                when (selectedTab) {
                    0 -> AppearanceSettingsSection(settings = settings, onUpdate = { viewModel.updateSettings(it) })
                    1 -> IconAndHomeSettingsSection(settings = settings, iconPacks = iconPacks, onApplyIconPack = { viewModel.applyIconPack(it) }, onUpdate = { viewModel.updateSettings(it) })
                    2 -> WallpaperSettingsSection(settings = settings, onPickCustomWallpaper = onPickCustomWallpaper, onUpdate = { viewModel.updateSettings(it) })
                    3 -> DockAndIslandSettingsSection(settings = settings, onUpdate = { viewModel.updateSettings(it) })
                    4 -> SystemAndBackupSettingsSection(
                        isDefaultLauncher = isDefaultLauncher,
                        currentDefaultLauncherLabel = currentDefaultLauncherLabel,
                        onRequestSetDefaultLauncher = onRequestSetDefaultLauncher,
                        onOpenSystemHomeSettings = onOpenSystemHomeSettings,
                        onRefreshAppList = { viewModel.refreshInstalledApps(); viewModel.showMessage("Installed app list refreshed") },
                        onExportBackup = { exportBackupLauncher.launch("liquid_bubble_backup.json") },
                        onImportBackup = { importBackupLauncher.launch(arrayOf("application/json", "text/plain")) },
                        onResetSettings = {
                            scope.launch {
                                viewModel.preferencesRepo.resetAllSettings()
                                viewModel.showMessage("All settings reset to Liquid defaults")
                            }
                        }
                    )
                }
            }
        }
    }
}
