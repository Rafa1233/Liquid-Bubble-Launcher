package com.liquidbubble.launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.liquidbubble.launcher.launcher.LauncherScreen
import com.liquidbubble.launcher.launcher.LauncherUiEvent
import com.liquidbubble.launcher.launcher.LauncherViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    viewModel.openFolderId.value != null -> viewModel.openFolder(null)
                    viewModel.isIslandExpanded.value -> viewModel.setIslandExpanded(false)
                    viewModel.isAppDrawerVisible.value -> viewModel.setAppDrawerVisible(false)
                    viewModel.isEditMode.value -> viewModel.setEditMode(false)
                    else -> viewModel.onHomeButtonPressed()
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is LauncherUiEvent.RequestWindowBrightness) {
                        val attrs = window.attributes
                        attrs.screenBrightness = event.brightness
                        window.attributes = attrs
                    }
                }
            }
        }

        setContent {
            LauncherScreen(viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDefaultLauncherStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomeButtonPressed()
        }
    }
}
