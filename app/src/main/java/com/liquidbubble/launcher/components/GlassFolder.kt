package com.liquidbubble.launcher.components

import android.graphics.Bitmap
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.FolderItem
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassFolderTile(
    folder: FolderItem,
    appsByKey: Map<String, AppInfo>,
    appManager: AppManager,
    onOpenFolder: () -> Unit,
    onDeleteFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalLiquidSettings.current
    val view = LocalView.current
    val previewApps = remember(folder.appKeys, appsByKey) { folder.appKeys.mapNotNull { appsByKey[it] }.take(4) }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .semantics { contentDescription = "${folder.name} folder, ${folder.appKeys.size} apps" }
            .combinedClickable(
                onClick = {
                    if (settings.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onOpenFolder()
                },
                onLongClick = {
                    if (settings.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    menuOpen = true
                }
            )
    ) {
        Box {
            LiquidBubbleSurface(
                shape = RoundedCornerShape(settings.iconCornerRadiusDp.dp),
                glassAlphaOverride = 0.44f,
                borderWidth = 1.2.dp,
                modifier = Modifier.size(settings.iconSizeDp.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize().padding(7.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniPreviewIcon(previewApps.getOrNull(0), appManager, Modifier.size(20.dp))
                        MiniPreviewIcon(previewApps.getOrNull(1), appManager, Modifier.size(20.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniPreviewIcon(previewApps.getOrNull(2), appManager, Modifier.size(20.dp))
                        MiniPreviewIcon(previewApps.getOrNull(3), appManager, Modifier.size(20.dp))
                    }
                }
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Open Folder") }, onClick = { menuOpen = false; onOpenFolder() })
                DropdownMenuItem(text = { Text("Delete Folder") }, onClick = { menuOpen = false; onDeleteFolder() })
            }
        }
        if (settings.showAppLabels) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = folder.name,
                style = TextStyle(color = IceWhite, fontSize = settings.labelFontSizeSp.sp, fontWeight = FontWeight.Medium, shadow = Shadow(Color.Black.copy(alpha = 0.65f), Offset(0f, 1.5f), 4f)),
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = settings.iconSizeDp.dp + 14.dp)
            )
        }
    }
}

@Composable
private fun MiniPreviewIcon(app: AppInfo?, appManager: AppManager, modifier: Modifier = Modifier) {
    val settings = LocalLiquidSettings.current
    var bmp by remember(app?.componentKey, settings.activeIconPackPackage) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(app?.componentKey, settings.activeIconPackPackage) {
        if (app != null) bmp = appManager.getAppIconBitmap(app, settings.activeIconPackPackage, 72)
    }
    Box(modifier = modifier.clip(RoundedCornerShape(6.dp)).background(if (app != null) Color.White.copy(alpha = 0.08f) else Color.Transparent)) {
        val loaded = bmp
        if (loaded != null) Image(bitmap = loaded.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun GlassFolderOverlay(
    folder: FolderItem?,
    allApps: List<AppInfo>,
    appsByKey: Map<String, AppInfo>,
    badgeCounts: Map<String, Int>,
    appManager: AppManager,
    onDismiss: () -> Unit,
    onLaunchApp: (AppInfo, View) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onAddAppToFolder: (String, String) -> Unit,
    onRemoveAppFromFolder: (String, String) -> Unit,
    onReorderAppInFolder: (String, Int, Int) -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    val palette = LocalLiquidPalette.current
    AnimatedVisibility(
        visible = folder != null,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.86f, animationSpec = tween(220)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.88f, animationSpec = tween(160))
    ) {
        if (folder == null) return@AnimatedVisibility
        val folderApps = remember(folder.appKeys, appsByKey) { folder.appKeys.mapNotNull { appsByKey[it] } }
        var isEditingName by remember(folder.id) { mutableStateOf(false) }
        var draftName by remember(folder.id, folder.name) { mutableStateOf(folder.name) }
        var showAddPicker by remember(folder.id) { mutableStateOf(false) }
        var contextAppIndex by remember { mutableStateOf<Int?>(null) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                .padding(horizontal = 20.dp, vertical = 48.dp)
        ) {
            LiquidBubbleSurface(
                shape = RoundedCornerShape(34.dp),
                glassAlphaOverride = 0.62f,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        if (isEditingName) {
                            BasicTextField(
                                value = draftName, onValueChange = { draftName = it }, singleLine = true,
                                cursorBrush = SolidColor(palette.accentPrimary),
                                textStyle = TextStyle(color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            IconButton(onClick = { onRenameFolder(folder.id, draftName); isEditingName = false }) {
                                Icon(Icons.Rounded.Check, contentDescription = "Save folder name", tint = IceWhite)
                            }
                        } else {
                            Text(folder.name, color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).clickable { isEditingName = true })
                            Row {
                                IconButton(onClick = { isEditingName = true }) { Icon(Icons.Rounded.Edit, contentDescription = "Rename folder", tint = IceWhite) }
                                IconButton(onClick = { showAddPicker = !showAddPicker }) { Icon(Icons.Rounded.Add, contentDescription = "Add app to folder", tint = IceWhite) }
                                IconButton(onClick = { onDeleteFolder(folder.id) }) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete folder", tint = IceWhite) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (showAddPicker) {
                        val available = remember(allApps, folder.appKeys) { allApps.filter { it.componentKey !in folder.appKeys } }
                        Text("Tap an app to add to ${folder.name}", color = palette.textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                            items(available, key = { it.componentKey }) { app ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onAddAppToFolder(folder.id, app.componentKey); showAddPicker = false }.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(app.label, color = IceWhite, fontSize = 14.sp)
                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.rimCyanTint)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showAddPicker = false }) { Text("Back to Folder", color = palette.rimCyanTint) }
                        }
                    } else if (folderApps.isEmpty()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(160.dp)) {
                            Text("Folder is empty. Tap + above to add apps.", color = palette.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)
                        ) {
                            itemsIndexed(folderApps, key = { _, app -> app.componentKey }) { index, app ->
                                Box(contentAlignment = Alignment.Center) {
                                    LiquidAppIcon(
                                        app = app, appManager = appManager, badgeCount = badgeCounts[app.packageName] ?: 0,
                                        onClick = { view -> onDismiss(); onLaunchApp(app, view) },
                                        onLongClick = { contextAppIndex = index }
                                    )
                                    DropdownMenu(expanded = contextAppIndex == index, onDismissRequest = { contextAppIndex = null }) {
                                        if (index > 0) DropdownMenuItem(text = { Text("Move Earlier") }, onClick = { contextAppIndex = null; onReorderAppInFolder(folder.id, index, index - 1) })
                                        if (index < folderApps.lastIndex) DropdownMenuItem(text = { Text("Move Later") }, onClick = { contextAppIndex = null; onReorderAppInFolder(folder.id, index, index + 1) })
                                        DropdownMenuItem(text = { Text("Remove from Folder") }, onClick = { contextAppIndex = null; onRemoveAppFromFolder(folder.id, app.componentKey) })
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
