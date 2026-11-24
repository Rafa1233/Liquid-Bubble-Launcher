package com.liquidbubble.launcher.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val componentKey: String,
    val category: AppCategory,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val isSystemApp: Boolean
)

@Serializable
enum class AppCategory(val displayName: String) {
    SOCIAL("Social"),
    GAMES("Games"),
    PRODUCTIVITY("Productivity"),
    MEDIA("Media"),
    TOOLS("Tools"),
    OTHER("Other")
}

@Serializable
enum class AppSortOrder(val label: String) {
    ALPHABETICAL("A–Z"),
    RECENTLY_INSTALLED("Recently Installed"),
    FREQUENTLY_USED("Frequently Used"),
    FAVORITES_FIRST("Favorites First")
}

@Serializable
enum class LauncherThemePreset(val displayName: String) {
    TITANIUM_DARK("Titanium Dark"),
    CRYSTAL_WHITE("Crystal White"),
    MIDNIGHT_BLUE("Midnight Blue"),
    AURORA_PURPLE("Aurora Purple"),
    SUNSET_GLASS("Sunset Glass"),
    OCEAN_GLASS("Ocean Glass"),
    CUSTOM("Custom Theme")
}

@Serializable
enum class PerformanceMode(val displayName: String, val subtitle: String) {
    PERFORMANCE("Performance", "Minimal blur & lightweight motion"),
    BALANCED("Balanced", "Moderate blur & smooth springs"),
    ULTRA_VISUAL("Ultra Visual", "Full refractive liquid glass & dynamic aura")
}

@Serializable
enum class WallpaperMode(val displayName: String) {
    TITANIUM_AURA("Titanium Mesh Aura"),
    BUILT_IN_PRESET("Built-in Liquid Preset"),
    CUSTOM_IMAGE("Custom Photo")
}

@Serializable
enum class WallpaperScaleMode(val displayName: String) {
    CROP("Fill Screen"),
    FIT("Fit Aspect Ratio")
}

@Serializable
enum class DockPosition(val displayName: String) {
    FLOATING_BOTTOM("Floating Capsule"),
    COMPACT_BOTTOM("Compact Edge")
}

@Serializable
enum class BuiltInWidgetType(val title: String) {
    CLOCK_WEATHER("Clock & Weather"),
    SYSTEM_MONITOR("Battery, RAM & Storage"),
    MUSIC("Now Playing"),
    CALENDAR("Upcoming Calendar")
}

@Immutable
@Serializable
data class FolderItem(
    val id: String,
    val name: String,
    val category: AppCategory = AppCategory.OTHER,
    val appKeys: List<String> = emptyList()
)

@Immutable
@Serializable
data class AndroidWidgetPlacement(
    val appWidgetId: Int,
    val providerPackage: String,
    val providerClass: String,
    val pageIndex: Int = 2,
    val spanX: Int = 4,
    val spanY: Int = 2,
    val label: String = ""
)

@Immutable
@Serializable
data class CustomShortcutItem(
    val id: String,
    val title: String,
    val actionUri: String,
    val packageName: String? = null
)

@Immutable
@Serializable
data class IconPackInfo(
    val packageName: String,
    val label: String
)

@Immutable
@Serializable
data class LauncherSettingsState(
    val themePreset: LauncherThemePreset = LauncherThemePreset.TITANIUM_DARK,
    val customAccentHex: Long = 0xFF2563EB,
    val glassAlpha: Float = 0.42f,
    val glassBlurDp: Float = 18f,
    val borderIntensity: Float = 0.65f,
    val glowStrength: Float = 0.55f,
    val iconSizeDp: Float = 60f,
    val iconCornerRadiusDp: Float = 16f,
    val labelFontSizeSp: Float = 11f,
    val showAppLabels: Boolean = true,
    val wallpaperMode: WallpaperMode = WallpaperMode.TITANIUM_AURA,
    val builtInWallpaperIndex: Int = 0,
    val customWallpaperUri: String? = null,
    val wallpaperScaleMode: WallpaperScaleMode = WallpaperScaleMode.CROP,
    val wallpaperBlurDp: Float = 0f,
    val wallpaperDimAlpha: Float = 0.12f,
    val auraAnimationSpeed: Float = 1.0f,
    val auraOrbSize: Float = 1.0f,
    val auraGlowStrength: Float = 0.85f,
    val auraMotionEnabled: Boolean = true,
    val homeColumns: Int = 4,
    val favoriteRows: Int = 2,
    val drawerColumns: Int = 4,
    val pageCount: Int = 3,
    val favoriteAppKeys: List<String> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val hiddenAppKeys: Set<String> = emptySet(),
    val appLaunchCounts: Map<String, Int> = emptyMap(),
    val recentAppKeys: List<String> = emptyList(),
    val dockAppKeys: List<String> = emptyList(),
    val dockHeightDp: Float = 86f,
    val dockHorizontalMarginDp: Float = 16f,
    val dockIconSizeDp: Float = 56f,
    val dockIconSpacingDp: Float = 12f,
    val dockMagnificationEnabled: Boolean = true,
    val dockAlpha: Float = 0.46f,
    val dockPosition: DockPosition = DockPosition.FLOATING_BOTTOM,
    val dynamicIslandEnabled: Boolean = true,
    val islandWidthFraction: Float = 0.56f,
    val islandHeightDp: Float = 38f,
    val islandWeatherVisible: Boolean = true,
    val islandBatteryVisible: Boolean = true,
    val islandMusicVisible: Boolean = true,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val batterySaverMode: Boolean = false,
    val reducedMotion: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val bubblePhysicsEnabled: Boolean = true,
    val folderAnimationEnabled: Boolean = true,
    val swipeUpForDrawer: Boolean = true,
    val swipeDownForSearch: Boolean = true,
    val longPressWallpaperForEdit: Boolean = true,
    val doubleTapLockOrQuickAction: Boolean = true,
    val activeIconPackPackage: String? = null,
    val customAppIconUris: Map<String, String> = emptyMap(),
    val applyIconMask: Boolean = true,
    val hostedWidgets: List<AndroidWidgetPlacement> = emptyList(),
    val customShortcuts: List<CustomShortcutItem> = emptyList(),
    val notificationBadgesEnabled: Boolean = true,
    val showBadgeCount: Boolean = true,
    val weatherCityName: String = "Jakarta",
    val weatherLatitude: Double = -6.2088,
    val weatherLongitude: Double = 106.8456,
    val useDeviceLocationForWeather: Boolean = false,
    val onboardingCompleted: Boolean = false
)

@Immutable
data class WeatherState(
    val temperatureCelsius: Double? = null,
    val conditionLabel: String = "Weather",
    val weatherCode: Int = -1,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Double? = null,
    val locationName: String = "Jakarta",
    val lastUpdatedEpochMs: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Immutable
data class BatteryState(
    val levelPercent: Int = -1,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float = 0f,
    val pluggedSource: String = "Battery"
)

@Immutable
data class SystemMetricsState(
    val totalRamBytes: Long = 0L,
    val availableRamBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val availableStorageBytes: Long = 0L
) {
    val usedRamFraction: Float
        get() = if (totalRamBytes > 0L) {
            ((totalRamBytes - availableRamBytes).toFloat() / totalRamBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val usedStorageFraction: Float
        get() = if (totalStorageBytes > 0L) {
            ((totalStorageBytes - availableStorageBytes).toFloat() / totalStorageBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

@Immutable
data class MediaPlaybackState(
    val hasActiveSession: Boolean = false,
    val packageName: String? = null,
    val title: String = "No Media Playing",
    val artist: String = "Tap play in a music app",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNotificationListenerAccess: Boolean = false
)

@Immutable
data class QuickControlsState(
    val isTorchOn: Boolean = false,
    val isTorchAvailable: Boolean = true,
    val isWifiConnected: Boolean = false,
    val wifiSsidOrStatus: String = "Wi-Fi",
    val isBluetoothOn: Boolean = false,
    val windowBrightness: Float = -1f
)

@Serializable
data class BackupPayload(
    val schemaVersion: Int = 1,
    val exportedAtEpochMs: Long,
    val settings: LauncherSettingsState
)
