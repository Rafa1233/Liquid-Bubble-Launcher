package com.liquidbubble.launcher.launcher

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquidbubble.launcher.components.BubbleDock
import com.liquidbubble.launcher.components.DynamicIsland
import com.liquidbubble.launcher.components.GlassFolderOverlay
import com.liquidbubble.launcher.data.model.AndroidWidgetPlacement
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.WallpaperMode
import com.liquidbubble.launcher.settings.SettingsScreen
import com.liquidbubble.launcher.system.AppWidgetHostManager
import com.liquidbubble.launcher.system.DeviceBatteryMonitor
import com.liquidbubble.launcher.system.DeviceMonitor
import com.liquidbubble.launcher.system.FlashlightManager
import com.liquidbubble.launcher.system.GeocodedLocation
import com.liquidbubble.launcher.system.LauncherMediaController
import com.liquidbubble.launcher.system.LiquidNotificationListenerService
import com.liquidbubble.launcher.system.WeatherRepository
import com.liquidbubble.launcher.ui.theme.LiquidBubbleTheme
import com.liquidbubble.launcher.wallpaper.WallpaperEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val drawerApps by viewModel.filteredDrawerApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showHiddenApps by viewModel.showHiddenApps.collectAsStateWithLifecycle()
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsStateWithLifecycle()
    val iconPacks by viewModel.iconPacks.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val openFolderId by viewModel.openFolderId.collectAsStateWithLifecycle()
    val isDrawerVisible by viewModel.isAppDrawerVisible.collectAsStateWithLifecycle()
    val drawerAutoFocus by viewModel.drawerAutoFocusSearch.collectAsStateWithLifecycle()
    val isIslandExpanded by viewModel.isIslandExpanded.collectAsStateWithLifecycle()
    val weather by viewModel.weatherState.collectAsStateWithLifecycle()
    val battery by viewModel.batteryState.collectAsStateWithLifecycle()
    val systemMetrics by viewModel.systemMetrics.collectAsStateWithLifecycle()
    val media by viewModel.mediaState.collectAsStateWithLifecycle()
    val quickControls by viewModel.quickControls.collectAsStateWithLifecycle()
    val badgeCounts by viewModel.notificationBadgeCounts.collectAsStateWithLifecycle()

    val batteryMonitor = remember(context) { DeviceBatteryMonitor(context) }
    val deviceMonitor = remember(context) { DeviceMonitor(context) }
    val flashlightManager = remember(context) { FlashlightManager(context) }
    val weatherRepository = remember(context) { WeatherRepository(context) }
    val mediaController = remember(context) { LauncherMediaController(context) }
    val appWidgetHostManager = remember(context) { AppWidgetHostManager(context) }

    var albumArt by remember { mutableStateOf<Bitmap?>(null) }
    var isResumed by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsStartTab by remember { mutableIntStateOf(0) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var dockReplaceSlotIndex by remember { mutableStateOf<Int?>(null) }
    var pendingCustomIconApp by remember { mutableStateOf<AppInfo?>(null) }
    var showCameraRationaleDialog by remember { mutableStateOf(false) }
    var pendingWidgetBindId by remember { mutableIntStateOf(-1) }
    var pendingWidgetProvider by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { settings.pageCount.coerceAtLeast(3) })

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> appWidgetHostManager.startListening()
                Lifecycle.Event.ON_STOP -> appWidgetHostManager.stopListening()
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE -> isResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            appWidgetHostManager.stopListening()
        }
    }

    LaunchedEffect(batteryMonitor) { batteryMonitor.observeBattery().collect { viewModel.publishBatteryState(it) } }
    LaunchedEffect(flashlightManager) {
        viewModel.publishQuickControls { it.copy(isTorchAvailable = flashlightManager.isAvailable()) }
        flashlightManager.observeTorchState().collect { on -> viewModel.publishQuickControls { it.copy(isTorchOn = on) } }
    }
    LaunchedEffect(Unit) { LiquidNotificationListenerService.badgeCounts.collect { counts -> viewModel.publishNotificationCounts(counts) } }

    LaunchedEffect(isResumed) {
        if (!isResumed) return@LaunchedEffect
        while (isActive) {
            viewModel.publishSystemMetrics(deviceMonitor.readSystemMetrics())
            val wifi = deviceMonitor.isWifiConnected()
            val bt = deviceMonitor.isBluetoothEnabled()
            viewModel.publishQuickControls { it.copy(isWifiConnected = wifi, isBluetoothOn = bt) }
            val (mediaInfo, art) = mediaController.refreshPlaybackState()
            viewModel.publishMediaState(mediaInfo)
            albumArt = art
            delay(if (isIslandExpanded) 1_500L else 6_000L)
        }
    }

    fun triggerWeatherRefresh() {
        scope.launch {
            val coords = if (settings.useDeviceLocationForWeather) weatherRepository.getLastKnownCoordinates() else null
            val lat = coords?.first ?: settings.weatherLatitude
            val lon = coords?.second ?: settings.weatherLongitude
            val label = if (coords != null) "Current Location" else settings.weatherCityName
            weatherRepository.fetchCurrentWeather(lat, lon, label)
                .onSuccess { viewModel.publishWeatherState(it) }
                .onFailure {
                    if (weather.temperatureCelsius == null) {
                        viewModel.publishWeatherState(weather.copy(locationName = label, errorMessage = "Offline · Tap to retry"))
                    }
                }
        }
    }

    LaunchedEffect(settings.weatherLatitude, settings.weatherLongitude, settings.useDeviceLocationForWeather) { triggerWeatherRefresh() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LauncherUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                LauncherUiEvent.ScrollHomeToMainPage -> { showSettings = false; pagerState.animateScrollToPage(0) }
                is LauncherUiEvent.RequestWindowBrightness -> Unit
            }
        }
    }

    val defaultLauncherPrompt = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { viewModel.refreshDefaultLauncherStatus() }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) flashlightManager.setTorchEnabled(!quickControls.isTorchOn).onFailure { viewModel.showMessage("Flashlight unavailable") }
        else viewModel.showMessage("Camera permission denied")
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) { viewModel.updateSettings { it.copy(useDeviceLocationForWeather = true) }; triggerWeatherRefresh() }
        else viewModel.showMessage("Using manual city weather")
    }
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            viewModel.updateSettings { it.copy(wallpaperMode = WallpaperMode.CUSTOM_IMAGE, customWallpaperUri = uri.toString()) }
        }
    }
    val customAppIconPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        val targetApp = pendingCustomIconApp; pendingCustomIconApp = null
        if (uri != null && targetApp != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            viewModel.setPerAppCustomIcon(targetApp.componentKey, uri.toString())
        }
    }
    val appWidgetBindLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val provider = pendingWidgetProvider; val widgetId = pendingWidgetBindId
        pendingWidgetProvider = null; pendingWidgetBindId = -1
        val targetPage = pagerState.currentPage.coerceAtLeast(2)
        if (result.resultCode == Activity.RESULT_OK && provider != null && widgetId != -1) {
            viewModel.addHostedWidget(AndroidWidgetPlacement(widgetId, provider.provider.packageName, provider.provider.className, targetPage, label = provider.loadLabel(context.packageManager)))
            viewModel.showMessage("Widget added to page ${targetPage + 1}")
        } else if (widgetId != -1) appWidgetHostManager.deleteWidgetId(widgetId)
    }

    fun handleToggleTorch() {
        if (!flashlightManager.isAvailable()) { viewModel.showMessage("Hardware flashlight not available"); return }
        if (!flashlightManager.hasCameraPermission()) { showCameraRationaleDialog = true; return }
        flashlightManager.setTorchEnabled(!quickControls.isTorchOn).onFailure { viewModel.showMessage("Unable to toggle flashlight") }
    }

    fun handleSelectAndroidWidget(provider: AppWidgetProviderInfo) {
        showWidgetPicker = false
        val widgetId = appWidgetHostManager.allocateWidgetId()
        val targetPage = pagerState.currentPage.coerceAtLeast(2)
        if (appWidgetHostManager.tryBindWidget(widgetId, provider)) {
            viewModel.addHostedWidget(AndroidWidgetPlacement(widgetId, provider.provider.packageName, provider.provider.className, targetPage, label = provider.loadLabel(context.packageManager)))
            viewModel.showMessage("Widget added to page ${targetPage + 1}")
        } else {
            pendingWidgetBindId = widgetId; pendingWidgetProvider = provider
            appWidgetBindLauncher.launch(appWidgetHostManager.createBindRequestIntent(widgetId, provider))
        }
    }

    val appsByKey = remember(installedApps) { installedApps.associateBy { it.componentKey } }
    val dockApps = remember(settings.dockAppKeys, appsByKey) { settings.dockAppKeys.mapNotNull { appsByKey[it] } }
    val activeFolder = remember(openFolderId, settings.folders) { settings.folders.firstOrNull { it.id == openFolderId } }

    LiquidBubbleTheme(settings = settings) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperEngine(
                onWallpaperDoubleTap = { if (settings.doubleTapLockOrQuickAction) viewModel.setIslandExpanded(!isIslandExpanded) },
                onWallpaperLongPress = { if (settings.longPressWallpaperForEdit) viewModel.setEditMode(!isEditMode) }
            )
            Column(modifier = Modifier.fillMaxSize()) {
                DynamicIsland(
                    isExpanded = isIslandExpanded, weather = weather, battery = battery, media = media, albumArt = albumArt, quickControls = quickControls,
                    onToggleExpanded = { viewModel.setIslandExpanded(it) },
                    onTogglePlayPause = {
                        mediaController.togglePlayPause()
                        scope.launch { delay(250); val (updated, art) = mediaController.refreshPlaybackState(); viewModel.publishMediaState(updated); albumArt = art }
                    },
                    onSkipNext = { mediaController.skipToNext() },
                    onSkipPrevious = { mediaController.skipToPrevious() },
                    onRequestNotificationAccess = { viewModel.openSystemSettingsIntent(mediaController.createNotificationListenerSettingsIntent()) },
                    onToggleTorch = ::handleToggleTorch,
                    onOpenWifiPanel = { viewModel.openSystemSettingsIntent(deviceMonitor.createWifiSettingsIntent()) },
                    onOpenBluetoothSettings = { viewModel.openSystemSettingsIntent(deviceMonitor.createBluetoothSettingsIntent()) },
                    onBrightnessChange = { viewModel.setWindowBrightness(it) }
                )
                HomeScreen(
                    pagerState = pagerState, installedApps = installedApps, appsByKey = appsByKey, badgeCounts = badgeCounts,
                    weather = weather, battery = battery, systemMetrics = systemMetrics, media = media, albumArt = albumArt,
                    isEditMode = isEditMode, appManager = viewModel.appManager, appWidgetHostManager = appWidgetHostManager,
                    onLaunchApp = { app, view -> viewModel.launchApp(app, view) },
                    onOpenDrawer = { autoFocus -> viewModel.setAppDrawerVisible(true, autoFocus) },
                    onOpenFolder = { viewModel.openFolder(it) },
                    onDeleteFolder = { viewModel.deleteFolder(it) },
                    onCreateFolderWithApp = { app -> viewModel.createFolder(app.category.displayName, listOf(app.componentKey)); viewModel.showMessage("Created folder") },
                    onAddAppToFolder = { fid, key -> viewModel.addAppToFolder(fid, key) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onAddToDock = { viewModel.addToDock(it) },
                    onOpenAppInfo = { viewModel.appManager.openAppInfo(it.packageName) },
                    onUninstallApp = { viewModel.appManager.requestUninstallApp(it.packageName) },
                    onOpenWeatherLocationDialog = { showWeatherDialog = true },
                    onRefreshWeather = ::triggerWeatherRefresh,
                    onTogglePlayPause = { mediaController.togglePlayPause() },
                    onSkipNext = { mediaController.skipToNext() },
                    onSkipPrevious = { mediaController.skipToPrevious() },
                    onRequestMediaAccess = { viewModel.openSystemSettingsIntent(mediaController.createNotificationListenerSettingsIntent()) },
                    onExitEditMode = { viewModel.setEditMode(false) },
                    onOpenAddWidgetPicker = { showWidgetPicker = true },
                    onOpenWallpaperSettings = { settingsStartTab = 2; showSettings = true },
                    onOpenSettings = { settingsStartTab = 0; showSettings = true },
                    onRemoveHostedWidget = { id -> appWidgetHostManager.deleteWidgetId(id); viewModel.removeHostedWidget(id) },
                    onResizeHostedWidget = { id, spanY -> viewModel.updateHostedWidgetSpan(id, 4, spanY) },
                    onOpenCalendarApp = {
                        val calIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
                        if (calIntent.resolveActivity(context.packageManager) != null) viewModel.openSystemSettingsIntent(calIntent)
                        else viewModel.openSystemSettingsIntent(Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()))
                    },
                    modifier = Modifier.weight(1f)
                )
                BubbleDock(
                    dockApps = dockApps, appManager = viewModel.appManager, badgeCounts = badgeCounts,
                    onLaunchApp = { app, view -> viewModel.launchApp(app, view) },
                    onReorderDock = { from, to -> viewModel.reorderDock(from, to) },
                    onRemoveFromDock = { app -> viewModel.removeFromDock(app.componentKey) },
                    onOpenAppInfo = { app -> viewModel.appManager.openAppInfo(app.packageName) },
                    onRequestReplaceSlot = { slotIdx -> dockReplaceSlotIndex = slotIdx }
                )
            }
            if (isIslandExpanded) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 290.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { viewModel.setIslandExpanded(false) })
            }
            AppDrawerScreen(
                visible = isDrawerVisible, autoFocusSearch = drawerAutoFocus, apps = drawerApps, searchQuery = searchQuery,
                sortOrder = sortOrder, selectedCategory = selectedCategory, showHiddenApps = showHiddenApps,
                folders = settings.folders, badgeCounts = badgeCounts, appManager = viewModel.appManager,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) }, onSortOrderChange = { viewModel.updateSortOrder(it) },
                onCategorySelect = { viewModel.selectCategory(it) }, onToggleShowHidden = { viewModel.toggleShowHiddenApps() },
                onDismiss = { viewModel.setAppDrawerVisible(false) },
                onLaunchApp = { app, view -> viewModel.setAppDrawerVisible(false); viewModel.launchApp(app, view) },
                onOpenAppInfo = { viewModel.appManager.openAppInfo(it.packageName) },
                onUninstallApp = { viewModel.appManager.requestUninstallApp(it.packageName) },
                onToggleFavorite = { viewModel.toggleFavorite(it) }, onAddToDock = { viewModel.addToDock(it) },
                onAddAppToFolder = { fid, key -> viewModel.addAppToFolder(fid, key) },
                onCreateFolderWithApp = { app -> viewModel.createFolder(app.category.displayName, listOf(app.componentKey)) },
                onToggleHideApp = { viewModel.toggleHiddenApp(it) },
                onPickCustomIconForApp = { app -> pendingCustomIconApp = app; customAppIconPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onResetCustomIconForApp = { app -> viewModel.setPerAppCustomIcon(app.componentKey, null) },
                onOpenSettings = { settingsStartTab = 0; showSettings = true }
            )
            GlassFolderOverlay(
                folder = activeFolder, allApps = installedApps, appsByKey = appsByKey, badgeCounts = badgeCounts, appManager = viewModel.appManager,
                onDismiss = { viewModel.openFolder(null) }, onLaunchApp = { app, view -> viewModel.launchApp(app, view) },
                onRenameFolder = { id, name -> viewModel.renameFolder(id, name) }, onAddAppToFolder = { id, key -> viewModel.addAppToFolder(id, key) },
                onRemoveAppFromFolder = { id, key -> viewModel.removeAppFromFolder(id, key) },
                onReorderAppInFolder = { id, from, to -> viewModel.reorderAppInFolder(id, from, to) }, onDeleteFolder = { id -> viewModel.deleteFolder(id) }
            )
            if (showSettings) {
                SettingsScreen(
                    viewModel = viewModel, initialTabIndex = settingsStartTab, isDefaultLauncher = isDefaultLauncher,
                    currentDefaultLauncherLabel = viewModel.roleManager.getCurrentDefaultLauncherLabel(), iconPacks = iconPacks,
                    onPickCustomWallpaper = { wallpaperPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRequestSetDefaultLauncher = { defaultLauncherPrompt.launch(viewModel.roleManager.createSetDefaultLauncherIntent()) },
                    onOpenSystemHomeSettings = { viewModel.openSystemSettingsIntent(viewModel.roleManager.createSystemHomeSettingsIntent()) },
                    onClose = { showSettings = false }
                )
            }
            if (!settings.onboardingCompleted) {
                OnboardingScreen(
                    installedApps = installedApps, appManager = viewModel.appManager, isDefaultLauncher = isDefaultLauncher,
                    favoriteKeys = settings.favoriteAppKeys,
                    onSetDefaultLauncher = { defaultLauncherPrompt.launch(viewModel.roleManager.createSetDefaultLauncherIntent()) },
                    onSelectThemePreset = { preset -> viewModel.updateSettings { it.copy(themePreset = preset) } },
                    onSelectBuiltInWallpaper = { idx -> viewModel.updateSettings { it.copy(wallpaperMode = WallpaperMode.BUILT_IN_PRESET, builtInWallpaperIndex = idx) } },
                    onUseAuraWallpaper = { viewModel.updateSettings { it.copy(wallpaperMode = WallpaperMode.TITANIUM_AURA) } },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }, onFinish = { viewModel.completeOnboarding() }
                )
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 110.dp))
        }
        if (showCameraRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showCameraRationaleDialog = false },
                title = { Text("Enable Flashlight Access") },
                text = { Text("Camera permission is needed solely to control the device hardware torch.") },
                confirmButton = { TextButton(onClick = { showCameraRationaleDialog = false; cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Allow") } },
                dismissButton = { TextButton(onClick = { showCameraRationaleDialog = false }) { Text("Cancel") } }
            )
        }
        if (showWidgetPicker) {
            val providers = remember { appWidgetHostManager.getInstalledProviders() }
            AlertDialog(
                onDismissRequest = { showWidgetPicker = false },
                title = { Text("Add Android Widget") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(providers) { info ->
                            val label = remember(info) { info.loadLabel(context.packageManager) }
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { handleSelectAndroidWidget(info) }.padding(vertical = 10.dp, horizontal = 8.dp)) {
                                Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(info.provider.packageName, fontSize = 11.sp)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showWidgetPicker = false }) { Text("Close") } }
            )
        }
        val slotIdx = dockReplaceSlotIndex
        if (slotIdx != null) {
            AlertDialog(
                onDismissRequest = { dockReplaceSlotIndex = null },
                title = { Text("Choose Dock App") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                        items(installedApps, key = { it.componentKey }) { app ->
                            Text(app.label, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().clickable { viewModel.replaceDockSlot(slotIdx, app.componentKey); dockReplaceSlotIndex = null }.padding(vertical = 10.dp, horizontal = 8.dp))
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { dockReplaceSlotIndex = null }) { Text("Cancel") } }
            )
        }
        if (showWeatherDialog) {
            WeatherLocationDialog(
                currentCity = settings.weatherCityName, weatherRepository = weatherRepository,
                onUseDeviceLocation = {
                    showWeatherDialog = false
                    if (weatherRepository.hasLocationPermission()) { viewModel.updateSettings { it.copy(useDeviceLocationForWeather = true) }; triggerWeatherRefresh() }
                    else locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                onSelectCity = { loc ->
                    showWeatherDialog = false
                    viewModel.updateSettings { it.copy(weatherCityName = loc.name, weatherLatitude = loc.latitude, weatherLongitude = loc.longitude, useDeviceLocationForWeather = false) }
                },
                onDismiss = { showWeatherDialog = false }
            )
        }
    }
}

@Composable
private fun WeatherLocationDialog(
    currentCity: String,
    weatherRepository: WeatherRepository,
    onUseDeviceLocation: () -> Unit,
    onSelectCity: (GeocodedLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(currentCity) }
    var results by remember { mutableStateOf<List<GeocodedLocation>>(emptyList()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weather Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onUseDeviceLocation) { Text("Use Approximate Device Location (Optional)") }
                OutlinedTextField(value = query, onValueChange = { query = it; scope.launch { results = weatherRepository.searchCity(it) } }, label = { Text("Search City") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(results) { item ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSelectCity(item) }.padding(vertical = 8.dp)) { Text("${item.name}, ${item.country}", fontSize = 13.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Powered by Open-Meteo (no API key required)", fontSize = 11.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
