package com.liquidbubble.launcher.launcher

import android.graphics.Bitmap
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassFolderTile
import com.liquidbubble.launcher.components.GlassWidget
import com.liquidbubble.launcher.components.LiquidAppIcon
import com.liquidbubble.launcher.components.LiquidBubbleSurface
import com.liquidbubble.launcher.components.PageIndicator
import com.liquidbubble.launcher.data.model.AppCategory
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.BatteryState
import com.liquidbubble.launcher.data.model.FolderItem
import com.liquidbubble.launcher.data.model.MediaPlaybackState
import com.liquidbubble.launcher.data.model.SystemMetricsState
import com.liquidbubble.launcher.data.model.WeatherState
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.system.AppWidgetHostManager
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
import com.liquidbubble.launcher.widgets.BatteryWidget
import com.liquidbubble.launcher.widgets.ClockWeatherWidget
import com.liquidbubble.launcher.widgets.MusicWidget
import com.liquidbubble.launcher.widgets.NativeAndroidWidgetView
import com.liquidbubble.launcher.widgets.SystemMonitorWidget
import com.liquidbubble.launcher.widgets.WeatherWidget
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    pagerState: PagerState,
    installedApps: List<AppInfo>,
    appsByKey: Map<String, AppInfo>,
    badgeCounts: Map<String, Int>,
    weather: WeatherState,
    battery: BatteryState,
    systemMetrics: SystemMetricsState,
    media: MediaPlaybackState,
    albumArt: Bitmap?,
    isEditMode: Boolean,
    appManager: AppManager,
    appWidgetHostManager: AppWidgetHostManager,
    onLaunchApp: (AppInfo, View) -> Unit,
    onOpenDrawer: (autoFocusSearch: Boolean) -> Unit,
    onOpenFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onCreateFolderWithApp: (AppInfo) -> Unit,
    onAddAppToFolder: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToDock: (String) -> Unit,
    onOpenAppInfo: (AppInfo) -> Unit,
    onUninstallApp: (AppInfo) -> Unit,
    onOpenWeatherLocationDialog: () -> Unit,
    onRefreshWeather: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onRequestMediaAccess: () -> Unit,
    onExitEditMode: () -> Unit,
    onOpenAddWidgetPicker: () -> Unit,
    onOpenWallpaperSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemoveHostedWidget: (Int) -> Unit,
    onResizeHostedWidget: (Int, Int) -> Unit,
    onOpenCalendarApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalLiquidSettings.current
    val scope = rememberCoroutineScope()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val adaptiveColumns = if (screenWidthDp >= 640) (settings.homeColumns + 2).coerceAtMost(6) else settings.homeColumns

    var verticalDragDelta by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(settings.swipeUpForDrawer, settings.swipeDownForSearch) {
                detectVerticalDragGestures(
                    onDragStart = { verticalDragDelta = 0f },
                    onVerticalDrag = { _, dragAmount -> verticalDragDelta += dragAmount },
                    onDragEnd = {
                        if (verticalDragDelta < -120f && settings.swipeUpForDrawer) onOpenDrawer(false)
                        else if (verticalDragDelta > 120f && settings.swipeDownForSearch) onOpenDrawer(true)
                        verticalDragDelta = 0f
                    }
                )
            }
    ) {
        AnimatedVisibility(visible = isEditMode) {
            HomeEditToolbar(
                onAddWidget = onOpenAddWidgetPicker,
                onWallpaper = onOpenWallpaperSettings,
                onSettings = onOpenSettings,
                onDone = onExitEditMode
            )
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
            when (page) {
                0 -> MainHomePage(
                    columns = adaptiveColumns,
                    appsByKey = appsByKey,
                    badgeCounts = badgeCounts,
                    weather = weather,
                    battery = battery,
                    systemMetrics = systemMetrics,
                    folders = settings.folders,
                    appManager = appManager,
                    onLaunchApp = onLaunchApp,
                    onOpenFolder = onOpenFolder,
                    onDeleteFolder = onDeleteFolder,
                    onCreateFolderWithApp = onCreateFolderWithApp,
                    onAddAppToFolder = onAddAppToFolder,
                    onToggleFavorite = onToggleFavorite,
                    onAddToDock = onAddToDock,
                    onOpenAppInfo = onOpenAppInfo,
                    onUninstallApp = onUninstallApp,
                    onWeatherClick = onOpenWeatherLocationDialog
                )
                1 -> AppLibraryPage(
                    installedApps = installedApps,
                    appsByKey = appsByKey,
                    badgeCounts = badgeCounts,
                    folders = settings.folders,
                    appManager = appManager,
                    onOpenSearch = { onOpenDrawer(true) },
                    onOpenAllApps = { onOpenDrawer(false) },
                    onOpenFolder = onOpenFolder,
                    onLaunchApp = onLaunchApp
                )
                else -> CustomWidgetsPage(
                    pageIndex = page,
                    weather = weather,
                    battery = battery,
                    media = media,
                    albumArt = albumArt,
                    isEditMode = isEditMode,
                    appWidgetHostManager = appWidgetHostManager,
                    onOpenWeatherLocationDialog = onOpenWeatherLocationDialog,
                    onRefreshWeather = onRefreshWeather,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onRequestMediaAccess = onRequestMediaAccess,
                    onOpenAddWidgetPicker = onOpenAddWidgetPicker,
                    onRemoveHostedWidget = onRemoveHostedWidget,
                    onResizeHostedWidget = onResizeHostedWidget,
                    onOpenCalendarApp = onOpenCalendarApp
                )
            }
        }

        PageIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage,
            onSelectPage = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun MainHomePage(
    columns: Int,
    appsByKey: Map<String, AppInfo>,
    badgeCounts: Map<String, Int>,
    weather: WeatherState,
    battery: BatteryState,
    systemMetrics: SystemMetricsState,
    folders: List<FolderItem>,
    appManager: AppManager,
    onLaunchApp: (AppInfo, View) -> Unit,
    onOpenFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onCreateFolderWithApp: (AppInfo) -> Unit,
    onAddAppToFolder: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToDock: (String) -> Unit,
    onOpenAppInfo: (AppInfo) -> Unit,
    onUninstallApp: (AppInfo) -> Unit,
    onWeatherClick: () -> Unit
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    val favoriteApps = remember(settings.favoriteAppKeys, appsByKey, settings.favoriteRows, columns) {
        settings.favoriteAppKeys.mapNotNull { appsByKey[it] }.take(columns * settings.favoriteRows)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        ClockWeatherWidget(weather = weather, onWeatherClick = onWeatherClick)
        Spacer(modifier = Modifier.height(14.dp))
        SystemMonitorWidget(battery = battery, metrics = systemMetrics)
        Spacer(modifier = Modifier.height(18.dp))

        if (favoriteApps.isNotEmpty()) {
            val rows = favoriteApps.chunked(columns)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                rows.forEach { rowApps ->
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        rowApps.forEach { app ->
                            var menuExpanded by remember(app.componentKey) { mutableStateOf(false) }
                            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.weight(1f)) {
                                LiquidAppIcon(
                                    app = app, appManager = appManager,
                                    badgeCount = badgeCounts[app.packageName] ?: 0,
                                    onClick = { view -> onLaunchApp(app, view) },
                                    onLongClick = { menuExpanded = true }
                                )
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                    DropdownMenuItem(text = { Text("App Info") }, onClick = { menuExpanded = false; onOpenAppInfo(app) })
                                    DropdownMenuItem(text = { Text("Remove from Favorites") }, onClick = { menuExpanded = false; onToggleFavorite(app.componentKey) })
                                    DropdownMenuItem(text = { Text("Add to Dock") }, onClick = { menuExpanded = false; onAddToDock(app.componentKey) })
                                    DropdownMenuItem(text = { Text("Create New Folder") }, onClick = { menuExpanded = false; onCreateFolderWithApp(app) })
                                    folders.firstOrNull()?.let { firstFolder ->
                                        DropdownMenuItem(text = { Text("Add to ${firstFolder.name}") }, onClick = { menuExpanded = false; onAddAppToFolder(firstFolder.id, app.componentKey) })
                                    }
                                    if (!app.isSystemApp) {
                                        DropdownMenuItem(text = { Text("Uninstall") }, onClick = { menuExpanded = false; onUninstallApp(app) })
                                    }
                                }
                            }
                        }
                        repeat(columns - rowApps.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }

        if (folders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Glass Folders", color = palette.textSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                folders.take(4).forEach { folder ->
                    GlassFolderTile(
                        folder = folder, appsByKey = appsByKey, appManager = appManager,
                        onOpenFolder = { onOpenFolder(folder.id) }, onDeleteFolder = { onDeleteFolder(folder.id) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun AppLibraryPage(
    installedApps: List<AppInfo>,
    appsByKey: Map<String, AppInfo>,
    badgeCounts: Map<String, Int>,
    folders: List<FolderItem>,
    appManager: AppManager,
    onOpenSearch: () -> Unit,
    onOpenAllApps: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onLaunchApp: (AppInfo, View) -> Unit
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    val visibleApps = remember(installedApps, settings.hiddenAppKeys) { installedApps.filter { it.componentKey !in settings.hiddenAppKeys } }
    val recentApps = remember(settings.recentAppKeys, appsByKey) { settings.recentAppKeys.mapNotNull { appsByKey[it] }.take(4) }
    val grouped = remember(visibleApps) { visibleApps.groupBy { it.category } }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LiquidBubbleSurface(shape = RoundedCornerShape(22.dp), modifier = Modifier.weight(1f).height(48.dp).clickable(onClick = onOpenSearch)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.textSecondary)
                        Text("Search App Library...", color = palette.textSecondary, fontSize = 14.sp)
                    }
                }
                LiquidBubbleSurface(shape = RoundedCornerShape(22.dp), modifier = Modifier.height(48.dp).clickable(onClick = onOpenAllApps)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 14.dp)) {
                        Icon(Icons.Rounded.Apps, contentDescription = "All Apps Drawer", tint = IceWhite, modifier = Modifier.size(18.dp))
                        Text("All", color = IceWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        if (recentApps.isNotEmpty()) {
            item {
                GlassWidget {
                    Column {
                        Text("Recently Opened", color = palette.rimCyanTint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            recentApps.forEach { app ->
                                LiquidAppIcon(app = app, appManager = appManager, iconSize = 54.dp, badgeCount = badgeCounts[app.packageName] ?: 0, onClick = { v -> onLaunchApp(app, v) })
                            }
                        }
                    }
                }
            }
        }
        if (folders.isNotEmpty()) {
            item {
                GlassWidget {
                    Column {
                        Text("Your Folders", color = IceWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                            folders.take(4).forEach { folder ->
                                GlassFolderTile(folder = folder, appsByKey = appsByKey, appManager = appManager, onOpenFolder = { onOpenFolder(folder.id) }, onDeleteFolder = {})
                            }
                        }
                    }
                }
            }
        }
        items(AppCategory.entries) { category ->
            val catApps = grouped[category].orEmpty()
            if (catApps.isNotEmpty()) {
                GlassWidget {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(category.displayName, color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("${catApps.size} apps", color = palette.textSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            catApps.take(4).forEach { app ->
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    LiquidAppIcon(app = app, appManager = appManager, iconSize = 52.dp, badgeCount = badgeCounts[app.packageName] ?: 0, onClick = { v -> onLaunchApp(app, v) })
                                }
                            }
                            repeat((4 - catApps.take(4).size).coerceAtLeast(0)) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomWidgetsPage(
    pageIndex: Int,
    weather: WeatherState,
    battery: BatteryState,
    media: MediaPlaybackState,
    albumArt: Bitmap?,
    isEditMode: Boolean,
    appWidgetHostManager: AppWidgetHostManager,
    onOpenWeatherLocationDialog: () -> Unit,
    onRefreshWeather: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onRequestMediaAccess: () -> Unit,
    onOpenAddWidgetPicker: () -> Unit,
    onRemoveHostedWidget: (Int) -> Unit,
    onResizeHostedWidget: (Int, Int) -> Unit,
    onOpenCalendarApp: () -> Unit
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    val pageWidgets = remember(settings.hostedWidgets, pageIndex) {
        settings.hostedWidgets.filter { it.pageIndex == pageIndex || (pageIndex == 2 && it.pageIndex < 2) }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (pageIndex == 2) {
            item { WeatherWidget(weather = weather, onChangeLocationClick = onOpenWeatherLocationDialog, onRefreshClick = onRefreshWeather) }
            item { MusicWidget(media = media, albumArt = albumArt, onTogglePlayPause = onTogglePlayPause, onSkipNext = onSkipNext, onSkipPrevious = onSkipPrevious, onRequestAccess = onRequestMediaAccess) }
            item { BatteryWidget(battery = battery) }
            item { SpatialCalendarWidget(onOpenCalendarApp = onOpenCalendarApp) }
        }
        items(pageWidgets, key = { it.appWidgetId }) { placement ->
            NativeAndroidWidgetView(
                placement = placement, hostManager = appWidgetHostManager, isEditMode = isEditMode,
                onResizeHeight = { newSpanY -> onResizeHostedWidget(placement.appWidgetId, newSpanY) },
                onRemove = { onRemoveHostedWidget(placement.appWidgetId) }
            )
        }
        item {
            LiquidBubbleSurface(shape = RoundedCornerShape(24.dp), glassAlphaOverride = 0.34f, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenAddWidgetPicker)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.rimCyanTint)
                    Text(" Add Android System Widget", color = IceWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SpatialCalendarWidget(onOpenCalendarApp: () -> Unit) {
    val palette = LocalLiquidPalette.current
    val today = remember { LocalDate.now() }
    val month = remember { YearMonth.from(today) }
    val monthTitle = remember(today) { today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())) }

    GlassWidget(modifier = Modifier.clickable(onClick = onOpenCalendarApp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = palette.rimCyanTint, modifier = Modifier.size(18.dp))
                    Text(monthTitle, color = IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Open Calendar", color = palette.rimCyanTint, fontSize = 11.5.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                    Text(d, color = palette.textSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            val firstDayOffset = month.atDay(1).dayOfWeek.value - 1
            val totalDays = month.lengthOfMonth()
            val cells = buildList<Int?> {
                repeat(firstDayOffset) { add(null) }
                for (day in 1..totalDays) add(day)
                while (size % 7 != 0) add(null)
            }
            cells.chunked(7).forEach { week ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    week.forEach { dayNum ->
                        val isToday = dayNum == today.dayOfMonth
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).height(24.dp)) {
                            if (dayNum != null) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(22.dp).clip(CircleShape).background(if (isToday) palette.accentPrimary else Color.Transparent)) {
                                    Text(dayNum.toString(), color = if (isToday) IceWhite else palette.textSecondary, fontSize = 11.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEditToolbar(onAddWidget: () -> Unit, onWallpaper: () -> Unit, onSettings: () -> Unit, onDone: () -> Unit) {
    val palette = LocalLiquidPalette.current
    LiquidBubbleSurface(shape = RoundedCornerShape(999.dp), glassAlphaOverride = 0.64f, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            EditActionChip(Icons.Rounded.Widgets, "Widget", onAddWidget)
            EditActionChip(Icons.Rounded.Wallpaper, "Wallpaper", onWallpaper)
            EditActionChip(Icons.Rounded.Settings, "Settings", onSettings)
            Row(
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clip(CircleShape).background(palette.accentPrimary).clickable(onClick = onDone).padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = "Done editing", tint = IceWhite, modifier = Modifier.size(16.dp))
                Text("Done", color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EditActionChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = IceWhite, modifier = Modifier.size(15.dp))
        Text(label, color = IceWhite, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
