package com.liquidbubble.launcher.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.liquidbubble.launcher.animation.BubblePhysics
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.DockPosition
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings

@Composable
fun BubbleDock(
    dockApps: List<AppInfo>,
    appManager: AppManager,
    badgeCounts: Map<String, Int>,
    modifier: Modifier = Modifier,
    onLaunchApp: (AppInfo, View) -> Unit,
    onReorderDock: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemoveFromDock: (AppInfo) -> Unit,
    onOpenAppInfo: (AppInfo) -> Unit,
    onRequestReplaceSlot: (slotIndex: Int) -> Unit
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    val density = LocalDensity.current
    val view = LocalView.current

    var pressedIndex by remember { mutableStateOf<Int?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
    var contextMenuIndex by remember { mutableIntStateOf(-1) }

    val bottomMargin = when (settings.dockPosition) {
        DockPosition.FLOATING_BOTTOM -> 14.dp
        DockPosition.COMPACT_BOTTOM -> 4.dp
    }
    val horizontalMargin = settings.dockHorizontalMarginDp.dp
    val neighborSpreadPx = with(density) { 10.dp.toPx() }
    val slotStepPx = with(density) { (settings.dockIconSizeDp + settings.dockIconSpacingDp).dp.toPx() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalMargin)
            .padding(bottom = bottomMargin)
    ) {
        LiquidBubbleSurface(
            shape = RoundedCornerShape(34.dp),
            glassAlphaOverride = settings.dockAlpha,
            glowColorOverride = palette.accentSecondary,
            modifier = Modifier.fillMaxWidth().height(settings.dockHeightDp.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(settings.dockIconSpacingDp.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                dockApps.forEachIndexed { index, app ->
                    val physics = BubblePhysics.computeDockItemTransform(
                        index = index,
                        activeIndex = draggingIndex ?: pressedIndex,
                        magnificationEnabled = settings.dockMagnificationEnabled && settings.bubblePhysicsEnabled,
                        reducedMotion = settings.reducedMotion,
                        neighborSpreadPx = neighborSpreadPx
                    )

                    Box(contentAlignment = Alignment.Center) {
                        LiquidAppIcon(
                            app = app,
                            appManager = appManager,
                            iconSize = settings.dockIconSizeDp.dp,
                            showLabel = false,
                            badgeCount = badgeCounts[app.packageName] ?: 0,
                            externalScale = physics.scale,
                            externalTranslationX = physics.translationX,
                            externalTranslationY = physics.translationY,
                            onPressStateChanged = { pressed ->
                                pressedIndex = if (pressed) index else if (pressedIndex == index) null else pressedIndex
                            },
                            onClick = { v -> onLaunchApp(app, v) },
                            onLongClick = { contextMenuIndex = index },
                            modifier = Modifier.pointerInput(app.componentKey, dockApps.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragAccumulatedX = 0f
                                        contextMenuIndex = -1
                                        if (settings.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    },
                                    onDragEnd = { draggingIndex = null; dragAccumulatedX = 0f },
                                    onDragCancel = { draggingIndex = null; dragAccumulatedX = 0f },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val current = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                        dragAccumulatedX += dragAmount.x
                                        if (dragAccumulatedX > slotStepPx * 0.65f && current < dockApps.lastIndex) {
                                            onReorderDock(current, current + 1)
                                            draggingIndex = current + 1
                                            dragAccumulatedX = 0f
                                        } else if (dragAccumulatedX < -slotStepPx * 0.65f && current > 0) {
                                            onReorderDock(current, current - 1)
                                            draggingIndex = current - 1
                                            dragAccumulatedX = 0f
                                        }
                                    }
                                )
                            }
                        )

                        DropdownMenu(expanded = contextMenuIndex == index, onDismissRequest = { contextMenuIndex = -1 }) {
                            DropdownMenuItem(text = { Text("App Info") }, onClick = { contextMenuIndex = -1; onOpenAppInfo(app) })
                            DropdownMenuItem(text = { Text("Replace Dock App") }, onClick = { contextMenuIndex = -1; onRequestReplaceSlot(index) })
                            DropdownMenuItem(text = { Text("Remove from Dock") }, onClick = { contextMenuIndex = -1; onRemoveFromDock(app) })
                        }
                    }
                }
            }
        }
    }
}
