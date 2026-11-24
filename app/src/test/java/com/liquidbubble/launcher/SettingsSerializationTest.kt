package com.liquidbubble.launcher

import com.liquidbubble.launcher.data.model.AppCategory
import com.liquidbubble.launcher.data.model.BackupPayload
import com.liquidbubble.launcher.data.model.FolderItem
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.LauncherThemePreset
import com.liquidbubble.launcher.data.model.PerformanceMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsSerializationTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `backup payload round-trip preserves theme, folders, and dock`() {
        val original = LauncherSettingsState(
            themePreset = LauncherThemePreset.AURORA_PURPLE,
            glassAlpha = 0.52f,
            performanceMode = PerformanceMode.ULTRA_VISUAL,
            dockAppKeys = listOf("com.example.dialer/Main", "com.example.cam/Cam"),
            folders = listOf(FolderItem("f1", "Media", AppCategory.MEDIA, listOf("com.example.music/Main"))),
            onboardingCompleted = true
        )
        val payload = BackupPayload(1, 1_760_000_000_000L, original)
        val decoded = json.decodeFromString<BackupPayload>(json.encodeToString(payload))
        assertEquals(1, decoded.schemaVersion)
        assertEquals(original, decoded.settings)
    }
}
