package com.liquidbubble.launcher.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.animation.LiquidSprings
import com.liquidbubble.launcher.ui.theme.IceWhite

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    onSearchClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        if (onSearchClick != null) {
            LiquidBubbleSurface(
                shape = RoundedCornerShape(999.dp), glassAlphaOverride = 0.42f, borderWidth = 1.dp, enableGlow = false,
                modifier = Modifier.clickable(onClick = onSearchClick)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Rounded.Search, contentDescription = "Cari aplikasi", tint = IceWhite, modifier = Modifier.size(14.dp))
                    Text("Cari", color = IceWhite, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        LiquidBubbleSurface(shape = RoundedCornerShape(999.dp), glassAlphaOverride = 0.34f, borderWidth = 1.dp, enableGlow = false) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) {
                repeat(pageCount) { index ->
                    val isSelected = index == currentPage
                    val width by animateDpAsState(if (isSelected) 18.dp else 6.dp, LiquidSprings.DynamicIslandDpSpec, label = "dotWidth")
                    val color by animateColorAsState(if (isSelected) IceWhite else IceWhite.copy(alpha = 0.35f), label = "dotColor")
                    Box(
                        modifier = Modifier.height(6.dp).width(width).clip(CircleShape).background(color)
                            .clickable { onSelectPage(index) }.semantics { contentDescription = "Go to page ${index + 1}" }
                    )
                }
            }
        }
    }
}
