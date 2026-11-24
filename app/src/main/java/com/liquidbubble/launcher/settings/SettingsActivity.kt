package com.liquidbubble.launcher.settings

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquidbubble.launcher.data.model.WallpaperMode
import com.liquidbubble.launcher.launcher.LauncherViewModel
import com.liquidbubble.launcher.ui.theme.LiquidBubbleTheme

class SettingsActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isDefault by viewModel.isDefaultLauncher.collectAsStateWithLifecycle()
            val iconPacks by viewModel.iconPacks.collectAsStateWithLifecycle()

            val defaultLauncherPrompt = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                viewModel.refreshDefaultLauncherStatus()
            }
            val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    viewModel.updateSettings { it.copy(wallpaperMode = WallpaperMode.CUSTOM_IMAGE, customWallpaperUri = uri.toString()) }
                }
            }

            LiquidBubbleTheme(settings = settings) {
                SettingsScreen(
                    viewModel = viewModel,
                    isDefaultLauncher = isDefault,
                    currentDefaultLauncherLabel = viewModel.roleManager.getCurrentDefaultLauncherLabel(),
                    iconPacks = iconPacks,
                    onPickCustomWallpaper = { wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRequestSetDefaultLauncher = { defaultLauncherPrompt.launch(viewModel.roleManager.createSetDefaultLauncherIntent()) },
                    onOpenSystemHomeSettings = { viewModel.openSystemSettingsIntent(viewModel.roleManager.createSystemHomeSettingsIntent()) },
                    onClose = { finish() }
                )
            }
        }
    }
}
