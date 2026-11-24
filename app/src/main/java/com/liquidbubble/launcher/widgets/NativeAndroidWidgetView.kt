package com.liquidbubble.launcher.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.liquidbubble.launcher.components.LiquidBubbleSurface
import com.liquidbubble.launcher.data.model.AndroidWidgetPlacement
import com.liquidbubble.launcher.system.AppWidgetHostManager
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette

@Composable
fun NativeAndroidWidgetView(
    placement: AndroidWidgetPlacement,
    hostManager: AppWidgetHostManager,
    isEditMode: Boolean,
    onResizeHeight: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val providerInfo = remember(placement.appWidgetId) { hostManager.getProviderInfo(placement.appWidgetId) }
    val heightDp = (placement.spanY.coerceIn(1, 5) * 92).dp

    LiquidBubbleSurface(shape = RoundedCornerShape(26.dp), glassAlphaOverride = 0.24f, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isEditMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Android Widget · ${placement.label.ifBlank { placement.providerPackage }}",
                        color = palette.rimCyanTint,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(onClick = { onResizeHeight((placement.spanY - 1).coerceAtLeast(1)) }) { Icon(Icons.Rounded.Remove, contentDescription = "Shrink widget", tint = IceWhite) }
                        IconButton(onClick = { onResizeHeight((placement.spanY + 1).coerceAtMost(5)) }) { Icon(Icons.Rounded.Add, contentDescription = "Expand widget", tint = IceWhite) }
                        IconButton(onClick = onRemove) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove Android widget", tint = IceWhite) }
                    }
                }
            }
            if (providerInfo == null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp)) {
                    Text("Widget unavailable (app may have been uninstalled)", color = palette.textSecondary, fontSize = 12.sp)
                }
            } else {
                AndroidView(
                    factory = { hostManager.createHostView(placement.appWidgetId, providerInfo) },
                    modifier = Modifier.fillMaxWidth().height(heightDp).clip(RoundedCornerShape(24.dp))
                )
            }
        }
    }
}
