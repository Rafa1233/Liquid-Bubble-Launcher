package com.liquidbubble.launcher.launcher

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liquidbubble.launcher.data.model.AndroidWidgetPlacement
import com.liquidbubble.launcher.data.model.AppCategory
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.AppSortOrder
import com.liquidbubble.launcher.data.model.BatteryState
import com.liquidbubble.launcher.data.model.FolderItem
import com.liquidbubble.launcher.data.model.IconPackInfo
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.MediaPlaybackState
import com.liquidbubble.launcher.data.model.QuickControlsState
import com.liquidbubble.launcher.data.model.SystemMetricsState
import com.liquidbubble.launcher.data.model.WeatherState
import com.liquidbubble.launcher.data.preferences.LauncherPreferencesRepository
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.system.LauncherRoleManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface LauncherUiEvent {
    data class ShowMessage(val message: String) : LauncherUiEvent
    data class RequestWindowBrightness(val brightness: Float) : LauncherUiEvent
    data object ScrollHomeToMainPage : LauncherUiEvent
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesRepo = LauncherPreferencesRepository(application)
    val appManager = AppManager(application)
    val roleManager = LauncherRoleManager(application)

    val settings: StateFlow<LauncherSettingsState> = preferencesRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettingsState())

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(AppSortOrder.ALPHABETICAL)
    val sortOrder: StateFlow<AppSortOrder> = _sortOrder.asStateFlow()

    private val _selectedCategory = MutableStateFlow<AppCategory?>(null)
    val selectedCategory: StateFlow<AppCategory?> = _selectedCategory.asStateFlow()

    private val _showHiddenApps = MutableStateFlow(false)
    val showHiddenApps: StateFlow<Boolean> = _showHiddenApps.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(roleManager.isDefaultLauncher())
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _iconPacks = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val iconPacks: StateFlow<List<IconPackInfo>> = _iconPacks.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _openFolderId = MutableStateFlow<String?>(null)
    val openFolderId: StateFlow<String?> = _openFolderId.asStateFlow()

    private val _isAppDrawerVisible = MutableStateFlow(false)
    val isAppDrawerVisible: StateFlow<Boolean> = _isAppDrawerVisible.asStateFlow()

    private val _drawerAutoFocusSearch = MutableStateFlow(false)
    val drawerAutoFocusSearch: StateFlow<Boolean> = _drawerAutoFocusSearch.asStateFlow()

    private val _isIslandExpanded = MutableStateFlow(false)
    val isIslandExpanded: StateFlow<Boolean> = _isIslandExpanded.asStateFlow()

    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _batteryState = MutableStateFlow(BatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val _systemMetrics = MutableStateFlow(SystemMetricsState())
    val systemMetrics: StateFlow<SystemMetricsState> = _systemMetrics.asStateFlow()

    private val _mediaState = MutableStateFlow(MediaPlaybackState())
    val mediaState: StateFlow<MediaPlaybackState> = _mediaState.asStateFlow()

    private val _quickControls = MutableStateFlow(QuickControlsState())
    val quickControls: StateFlow<QuickControlsState> = _quickControls.asStateFlow()

    private val _notificationBadgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val notificationBadgeCounts: StateFlow<Map<String, Int>> = _notificationBadgeCounts.asStateFlow()

    private val _events = MutableSharedFlow<LauncherUiEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<LauncherUiEvent> = _events.asSharedFlow()

    val filteredDrawerApps: StateFlow<List<AppInfo>> = combine(
        combine(_installedApps, settings, _searchQuery, ::Triple),
        combine(_sortOrder, _selectedCategory, _showHiddenApps, ::Triple)
    ) { (apps, cfg, query), (sort, category, showHidden) ->
        val trimmed = query.trim().lowercase()
        val visible = apps.filter { app ->
            val hiddenMatch = showHidden || app.componentKey !in cfg.hiddenAppKeys
            val categoryMatch = category == null || app.category == category
            val queryMatch = trimmed.isEmpty() ||
                app.label.lowercase().contains(trimmed) ||
                app.packageName.lowercase().contains(trimmed)
            hiddenMatch && categoryMatch && queryMatch
        }
        when (sort) {
            AppSortOrder.ALPHABETICAL -> visible.sortedBy { it.label.lowercase() }
            AppSortOrder.RECENTLY_INSTALLED -> visible.sortedByDescending { it.firstInstallTime }
            AppSortOrder.FREQUENTLY_USED -> visible.sortedByDescending { cfg.appLaunchCounts[it.componentKey] ?: 0 }
            AppSortOrder.FAVORITES_FIRST -> visible.sortedWith(
                compareByDescending<AppInfo> { it.componentKey in cfg.favoriteAppKeys }.thenBy { it.label.lowercase() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refreshInstalledApps()
        viewModelScope.launch {
            appManager.observePackageChanges().collect { refreshInstalledApps() }
        }
        viewModelScope.launch {
            preferencesRepo.cachedWeatherFlow.first()?.let { cached ->
                if (_weatherState.value.temperatureCelsius == null) _weatherState.value = cached
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = appManager.loadInstalledApps()
            _installedApps.value = apps
            _iconPacks.value = appManager.discoverIconPacks()
            val current = settings.first()
            if (current.dockAppKeys.isEmpty() && apps.isNotEmpty()) {
                val defaultDock = appManager.resolveDefaultDockKeys(apps)
                val defaultFavorites = apps.filter { it.componentKey !in defaultDock }.take(8).map { it.componentKey }
                val defaultFolders = buildStarterFolders(apps)
                preferencesRepo.updateSettings {
                    it.copy(
                        dockAppKeys = defaultDock,
                        favoriteAppKeys = it.favoriteAppKeys.ifEmpty { defaultFavorites },
                        folders = it.folders.ifEmpty { defaultFolders }
                    )
                }
            }
        }
    }

    private fun buildStarterFolders(apps: List<AppInfo>): List<FolderItem> {
        val grouped = apps.groupBy { it.category }
        val starter = mutableListOf<FolderItem>()
        listOf(AppCategory.SOCIAL, AppCategory.MEDIA, AppCategory.PRODUCTIVITY, AppCategory.TOOLS, AppCategory.GAMES).forEach { cat ->
            val keys = grouped[cat].orEmpty().take(6).map { it.componentKey }
            if (keys.size >= 2) starter.add(FolderItem("starter_${cat.name.lowercase()}", cat.displayName, cat, keys))
        }
        return starter
    }

    fun refreshDefaultLauncherStatus() { _isDefaultLauncher.value = roleManager.isDefaultLauncher() }

    fun onHomeButtonPressed() {
        _isAppDrawerVisible.value = false
        _drawerAutoFocusSearch.value = false
        _isIslandExpanded.value = false
        _openFolderId.value = null
        _isEditMode.value = false
        _searchQuery.value = ""
        _events.tryEmit(LauncherUiEvent.ScrollHomeToMainPage)
    }

    fun launchApp(app: AppInfo, sourceView: View? = null) {
        val result = appManager.launchApp(app, sourceView)
        if (result.isSuccess) {
            viewModelScope.launch {
                preferencesRepo.updateSettings { s ->
                    val counts = s.appLaunchCounts.toMutableMap()
                    counts[app.componentKey] = (counts[app.componentKey] ?: 0) + 1
                    val recents = (listOf(app.componentKey) + s.recentAppKeys).distinct().take(12)
                    s.copy(appLaunchCounts = counts, recentAppKeys = recents)
                }
            }
        } else {
            _events.tryEmit(LauncherUiEvent.ShowMessage("Unable to open ${app.label}"))
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSortOrder(order: AppSortOrder) { _sortOrder.value = order }
    fun selectCategory(category: AppCategory?) { _selectedCategory.value = category }
    fun toggleShowHiddenApps() { _showHiddenApps.value = !_showHiddenApps.value }

    fun setAppDrawerVisible(visible: Boolean, autoFocusSearch: Boolean = false) {
        _drawerAutoFocusSearch.value = visible && autoFocusSearch
        _isAppDrawerVisible.value = visible
        if (!visible) _searchQuery.value = ""
    }

    fun setIslandExpanded(expanded: Boolean) { _isIslandExpanded.value = expanded }
    fun setEditMode(enabled: Boolean) { _isEditMode.value = enabled }
    fun openFolder(folderId: String?) { _openFolderId.value = folderId }

    fun toggleFavorite(componentKey: String) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                val list = s.favoriteAppKeys.toMutableList()
                if (componentKey in list) list.remove(componentKey) else list.add(componentKey)
                s.copy(favoriteAppKeys = list)
            }
        }
    }

    fun toggleHiddenApp(componentKey: String) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                val updated = s.hiddenAppKeys.toMutableSet()
                if (componentKey in updated) updated.remove(componentKey) else updated.add(componentKey)
                s.copy(hiddenAppKeys = updated)
            }
        }
    }

    fun addToDock(componentKey: String) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                if (componentKey in s.dockAppKeys) return@updateSettings s
                if (s.dockAppKeys.size >= 6) {
                    _events.tryEmit(LauncherUiEvent.ShowMessage("Dock holds up to 6 apps"))
                    return@updateSettings s
                }
                s.copy(dockAppKeys = s.dockAppKeys + componentKey)
            }
        }
    }

    fun removeFromDock(componentKey: String) {
        viewModelScope.launch { preferencesRepo.updateSettings { s -> s.copy(dockAppKeys = s.dockAppKeys - componentKey) } }
    }

    fun reorderDock(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                val list = s.dockAppKeys.toMutableList()
                if (fromIndex !in list.indices || toIndex !in list.indices) return@updateSettings s
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                s.copy(dockAppKeys = list)
            }
        }
    }

    fun replaceDockSlot(index: Int, newComponentKey: String) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                val list = s.dockAppKeys.toMutableList()
                if (index in list.indices) list[index] = newComponentKey else list.add(newComponentKey)
                s.copy(dockAppKeys = list.distinct())
            }
        }
    }

    fun createFolder(name: String, initialAppKeys: List<String>, category: AppCategory = AppCategory.OTHER) {
        viewModelScope.launch {
            val cleanName = name.trim().ifEmpty { "New Folder" }
            val folder = FolderItem(UUID.randomUUID().toString(), cleanName, category, initialAppKeys.distinct())
            preferencesRepo.updateSettings { s -> s.copy(folders = s.folders + folder) }
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val clean = newName.trim().ifEmpty { return@launch }
            preferencesRepo.updateSettings { s ->
                s.copy(folders = s.folders.map { if (it.id == folderId) it.copy(name = clean) else it })
            }
        }
    }

    fun addAppToFolder(folderId: String, componentKey: String) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                s.copy(folders = s.folders.map { if (it.id == folderId && componentKey !in it.appKeys) it.copy(appKeys = it.appKeys + componentKey) else it })
            }
        }
    }

    fun removeAppFromFolder(folderId: String, componentKey: String) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                s.copy(folders = s.folders.map { if (it.id == folderId) it.copy(appKeys = it.appKeys - componentKey) else it })
            }
        }
    }

    fun reorderAppInFolder(folderId: String, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                s.copy(
                    folders = s.folders.map { folder ->
                        if (folder.id == folderId) {
                            val list = folder.appKeys.toMutableList()
                            if (fromIndex in list.indices && toIndex in list.indices) {
                                val moved = list.removeAt(fromIndex)
                                list.add(toIndex, moved)
                            }
                            folder.copy(appKeys = list)
                        } else folder
                    }
                )
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            if (_openFolderId.value == folderId) _openFolderId.value = null
            preferencesRepo.updateSettings { s -> s.copy(folders = s.folders.filterNot { it.id == folderId }) }
        }
    }

    fun addHostedWidget(placement: AndroidWidgetPlacement) {
        viewModelScope.launch { preferencesRepo.updateSettings { s -> s.copy(hostedWidgets = s.hostedWidgets + placement) } }
    }

    fun removeHostedWidget(appWidgetId: Int) {
        viewModelScope.launch { preferencesRepo.updateSettings { s -> s.copy(hostedWidgets = s.hostedWidgets.filterNot { it.appWidgetId == appWidgetId }) } }
    }

    fun updateHostedWidgetSpan(appWidgetId: Int, spanX: Int, spanY: Int) {
        viewModelScope.launch {
            preferencesRepo.updateSettings { s ->
                s.copy(hostedWidgets = s.hostedWidgets.map { w ->
                    if (w.appWidgetId == appWidgetId) w.copy(spanX = spanX.coerceIn(1, 4), spanY = spanY.coerceIn(1, 5)) else w
                })
            }
        }
    }

    fun updateSettings(transform: (LauncherSettingsState) -> LauncherSettingsState) {
        viewModelScope.launch { preferencesRepo.updateSettings(transform) }
    }

    fun applyIconPack(packageName: String?) {
        appManager.clearIconCache()
        updateSettings { it.copy(activeIconPackPackage = packageName) }
    }

    fun setPerAppCustomIcon(componentKey: String, uriString: String?) {
        appManager.clearIconCache()
        updateSettings { s ->
            val map = s.customAppIconUris.toMutableMap()
            if (uriString == null) map.remove(componentKey) else map[componentKey] = uriString
            s.copy(customAppIconUris = map)
        }
    }

    fun completeOnboarding() { updateSettings { it.copy(onboardingCompleted = true) } }

    internal fun publishWeatherState(state: WeatherState) {
        _weatherState.value = state
        if (state.temperatureCelsius != null) viewModelScope.launch { preferencesRepo.saveCachedWeather(state) }
    }

    internal fun publishBatteryState(state: BatteryState) { _batteryState.value = state }
    internal fun publishSystemMetrics(state: SystemMetricsState) { _systemMetrics.value = state }
    internal fun publishMediaState(state: MediaPlaybackState) { _mediaState.value = state }
    internal fun publishQuickControls(transform: (QuickControlsState) -> QuickControlsState) { _quickControls.value = transform(_quickControls.value) }
    internal fun publishNotificationCounts(counts: Map<String, Int>) { _notificationBadgeCounts.value = counts }

    fun setWindowBrightness(value: Float) {
        _quickControls.value = _quickControls.value.copy(windowBrightness = value)
        _events.tryEmit(LauncherUiEvent.RequestWindowBrightness(value))
    }

    fun showMessage(msg: String) { _events.tryEmit(LauncherUiEvent.ShowMessage(msg)) }

    fun openSystemSettingsIntent(intent: Intent) {
        runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        }.onFailure { showMessage("Settings screen unavailable on this device") }
    }
}
