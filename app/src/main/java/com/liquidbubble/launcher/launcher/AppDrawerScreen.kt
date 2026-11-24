package com.liquidbubble.launcher.launcher

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.components.GlassSearchBar
import com.liquidbubble.launcher.components.LiquidAppIcon
import com.liquidbubble.launcher.components.LiquidBubbleSurface
import com.liquidbubble.launcher.data.model.AppCategory
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.AppSortOrder
import com.liquidbubble.launcher.data.model.FolderItem
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
import kotlinx.coroutines.delay

@Composable
fun AppDrawerScreen(
    visible: Boolean,
    autoFocusSearch: Boolean = false,
    apps: List<AppInfo>,
    searchQuery: String,
    sortOrder: AppSortOrder,
    selectedCategory: AppCategory?,
    showHiddenApps: Boolean,
    folders: List<FolderItem>,
    badgeCounts: Map<String, Int>,
    appManager: AppManager,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (AppSortOrder) -> Unit,
    onCategorySelect: (AppCategory?) -> Unit,
    onToggleShowHidden: () -> Unit,
    onDismiss: () -> Unit,
    onLaunchApp: (AppInfo, View) -> Unit,
    onOpenAppInfo: (AppInfo) -> Unit,
    onUninstallApp: (AppInfo) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToDock: (String) -> Unit,
    onAddAppToFolder: (String, String) -> Unit,
    onCreateFolderWithApp: (AppInfo) -> Unit,
    onToggleHideApp: (String) -> Unit,
    onPickCustomIconForApp: (AppInfo) -> Unit,
    onResetCustomIconForApp: (AppInfo) -> Unit,
    onOpenSettings: () -> Unit
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val columns = if (screenWidthDp >= 640) (settings.drawerColumns + 2).coerceAtMost(6) else settings.drawerColumns
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(visible, autoFocusSearch) {
        if (visible && autoFocusSearch) {
            delay(140)
            runCatching { searchFocusRequester.requestFocus() }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically(tween(240)) { it / 6 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(190)) { it / 6 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.backgroundBase.copy(alpha = 0.88f))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("App Library", color = IceWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = onOpenSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Liquid Settings", tint = IceWhite) }
                        IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close App Drawer", tint = IceWhite) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                GlassSearchBar(query = searchQuery, onQueryChange = onSearchQueryChange, focusRequester = searchFocusRequester)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    AppSortOrder.entries.forEach { order ->
                        DrawerFilterPill(order.label, sortOrder == order) { onSortOrderChange(order) }
                    }
                    DrawerFilterPill(if (showHiddenApps) "Showing Hidden" else "Show Hidden", showHiddenApps, onToggleShowHidden)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    DrawerFilterPill("All", selectedCategory == null) { onCategorySelect(null) }
                    AppCategory.entries.forEach { cat ->
                        DrawerFilterPill(cat.displayName, selectedCategory == cat) { onCategorySelect(cat) }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (apps.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LiquidBubbleSurface(modifier = Modifier.padding(24.dp)) {
                            Text("No matching apps found", color = palette.textSecondary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp))
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(apps, key = { it.componentKey }) { app ->
                            var menuOpen by remember(app.componentKey) { mutableStateOf(false) }
                            val isFavorite = app.componentKey in settings.favoriteAppKeys
                            val isHidden = app.componentKey in settings.hiddenAppKeys
                            val hasCustomIcon = settings.customAppIconUris.containsKey(app.componentKey)

                            Box(contentAlignment = Alignment.TopCenter) {
                                LiquidAppIcon(
                                    app = app, appManager = appManager, badgeCount = badgeCounts[app.packageName] ?: 0,
                                    onClick = { view -> onLaunchApp(app, view) },
                                    onLongClick = { menuOpen = true }
                                )
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                    DropdownMenuItem(text = { Text("App Info") }, onClick = { menuOpen = false; onOpenAppInfo(app) })
                                    DropdownMenuItem(text = { Text(if (isFavorite) "Remove Favorite" else "Add to Favorites") }, onClick = { menuOpen = false; onToggleFavorite(app.componentKey) })
                                    DropdownMenuItem(text = { Text("Add to Dock") }, onClick = { menuOpen = false; onAddToDock(app.componentKey) })
                                    DropdownMenuItem(text = { Text("Create Folder with App") }, onClick = { menuOpen = false; onCreateFolderWithApp(app) })
                                    folders.take(4).forEach { folder ->
                                        DropdownMenuItem(text = { Text("Add to ${folder.name}") }, onClick = { menuOpen = false; onAddAppToFolder(folder.id, app.componentKey) })
                                    }
                                    DropdownMenuItem(text = { Text("Custom Icon Image") }, onClick = { menuOpen = false; onPickCustomIconForApp(app) })
                                    if (hasCustomIcon) {
                                        DropdownMenuItem(text = { Text("Reset Icon") }, onClick = { menuOpen = false; onResetCustomIconForApp(app) })
                                    }
                                    DropdownMenuItem(text = { Text(if (isHidden) "Unhide App" else "Hide App") }, onClick = { menuOpen = false; onToggleHideApp(app.componentKey) })
                                    if (!app.isSystemApp) {
                                        DropdownMenuItem(text = { Text("Uninstall") }, onClick = { menuOpen = false; onUninstallApp(app) })
                                    }
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
private fun DrawerFilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalLiquidPalette.current
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) palette.accentPrimary.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(label, color = IceWhite, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
