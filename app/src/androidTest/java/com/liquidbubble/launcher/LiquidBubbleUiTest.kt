package com.liquidbubble.launcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.liquidbubble.launcher.components.DynamicIsland
import com.liquidbubble.launcher.components.GlassSearchBar
import com.liquidbubble.launcher.components.PageIndicator
import com.liquidbubble.launcher.data.model.BatteryState
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.MediaPlaybackState
import com.liquidbubble.launcher.data.model.QuickControlsState
import com.liquidbubble.launcher.data.model.WeatherState
import com.liquidbubble.launcher.ui.theme.LiquidBubbleTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LiquidBubbleUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun glassSearchBar_updatesQueryAndClears() {
        var query by mutableStateOf("")
        composeRule.setContent {
            LiquidBubbleTheme(settings = LauncherSettingsState()) {
                GlassSearchBar(query = query, onQueryChange = { query = it })
            }
        }
        composeRule.onNodeWithContentDescription("Search installed applications").performTextInput("camera")
        assertEquals("camera", query)
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        assertEquals("", query)
    }

    @Test
    fun pageIndicator_switchesPageOnTap() {
        var currentPage by mutableIntStateOf(0)
        composeRule.setContent {
            LiquidBubbleTheme(settings = LauncherSettingsState()) {
                PageIndicator(pageCount = 3, currentPage = currentPage, onSelectPage = { currentPage = it })
            }
        }
        composeRule.onNodeWithContentDescription("Go to page 3").performClick()
        assertEquals(2, currentPage)
    }

    @Test
    fun dynamicIsland_expandsOnTapAndShowsQuickControls() {
        var expanded by mutableStateOf(false)
        composeRule.setContent {
            LiquidBubbleTheme(settings = LauncherSettingsState()) {
                DynamicIsland(
                    isExpanded = expanded,
                    weather = WeatherState(29.0, "Clear Sky", locationName = "Jakarta"),
                    battery = BatteryState(85, false),
                    media = MediaPlaybackState(hasNotificationListenerAccess = true, hasActiveSession = false),
                    albumArt = null,
                    quickControls = QuickControlsState(),
                    onToggleExpanded = { expanded = it },
                    onTogglePlayPause = {}, onSkipNext = {}, onSkipPrevious = {},
                    onRequestNotificationAccess = {}, onToggleTorch = {},
                    onOpenWifiPanel = {}, onOpenBluetoothSettings = {}, onBrightnessChange = {}
                )
            }
        }
        composeRule.onNodeWithContentDescription("Dynamic Bubble Island").performClick()
        composeRule.onNodeWithText("No Active Playback").assertIsDisplayed()
        composeRule.onNodeWithText("Torch").assertIsDisplayed()
        composeRule.onNodeWithText("Wi-Fi").assertIsDisplayed()
        composeRule.onNodeWithText("Bluetooth").assertIsDisplayed()
    }
}
