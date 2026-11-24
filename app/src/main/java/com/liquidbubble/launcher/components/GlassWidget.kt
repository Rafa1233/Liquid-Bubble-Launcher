package com.liquidbubble.launcher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liquidbubble.launcher.ui.theme.IceWhite

@Composable
fun GlassWidget(
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onRemove: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        LiquidBubbleSurface(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
        }
        if (isEditMode && onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove widget", tint = IceWhite)
            }
        }
    }
}
