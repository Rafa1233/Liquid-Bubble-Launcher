package com.liquidbubble.launcher.components

import android.net.Uri
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.liquidbubble.launcher.animation.LiquidSprings
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.system.AppManager
import com.liquidbubble.launcher.ui.theme.IceWhite
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette
import com.liquidbubble.launcher.ui.theme.LocalLiquidSettings
import com.liquidbubble.launcher.ui.theme.SunsetCoral

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidAppIcon(
    app: AppInfo,
    appManager: AppManager,
    modifier: Modifier = Modifier,
    iconSize: Dp? = null,
    showLabel: Boolean? = null,
    badgeCount: Int = 0,
    externalScale: Float = 1f,
    externalTranslationX: Float = 0f,
    externalTranslationY: Float = 0f,
    onPressStateChanged: ((Boolean) -> Unit)? = null,
    onClick: (View) -> Unit,
    onLongClick: () -> Unit = {}
) {
    val settings = LocalLiquidSettings.current
    val palette = LocalLiquidPalette.current
    val view = LocalView.current
    val density = LocalDensity.current

    val finalIconSize = iconSize ?: settings.iconSizeDp.dp
    val finalShowLabel = showLabel ?: settings.showAppLabels
    val squircleShape = remember(settings.iconCornerRadiusDp) { RoundedCornerShape(settings.iconCornerRadiusDp.dp) }

    val customUri = settings.customAppIconUris[app.componentKey]
    val iconPx = with(density) { finalIconSize.roundToPx().coerceIn(72, 192) }
    var loadedImage by remember(app.componentKey, settings.activeIconPackPackage) {
        mutableStateOf<ImageBitmap?>(appManager.getCachedIconImage(app, settings.activeIconPackPackage))
    }

    LaunchedEffect(app.componentKey, settings.activeIconPackPackage, customUri, iconPx) {
        if (customUri == null && loadedImage == null) {
            loadedImage = appManager.getAppIconImageBitmap(app, settings.activeIconPackPackage, iconPx)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        onPressStateChanged?.invoke(isPressed)
        if (isPressed && settings.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    val targetScale = if (isPressed && !settings.reducedMotion) 1.12f else 1f
    val animatedScale by animateFloatAsState(targetScale * externalScale, LiquidSprings.BubbleBounceSpec, label = "iconScale")
    val animatedTx by animateFloatAsState(externalTranslationX, LiquidSprings.AdjacentShiftSpec, label = "iconTx")
    val animatedTy by animateFloatAsState(externalTranslationY, LiquidSprings.AdjacentShiftSpec, label = "iconTy")

    val bgBrush = remember(palette.glassBase) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.14f), palette.glassBase.copy(alpha = 0.45f)))
    }
    val borderBrush = remember(palette.rimCyanTint) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.44f), palette.rimCyanTint.copy(alpha = 0.12f), Color.White.copy(alpha = 0.22f)))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .semantics { contentDescription = app.label }
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale; translationX = animatedTx; translationY = animatedTy }
            .combinedClickable(
                interactionSource = interactionSource, indication = null,
                onClick = { onClick(view) },
                onLongClick = { if (settings.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); onLongClick() }
            )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(finalIconSize)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(squircleShape)
                    .background(bgBrush)
                    .border(0.9.dp, borderBrush, squircleShape)
            ) {
                if (customUri != null) {
                    AsyncImage(model = Uri.parse(customUri), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    val img = loadedImage
                    if (img != null) {
                        Image(bitmap = img, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(if (settings.applyIconMask) 2.5.dp else 0.dp))
                    } else {
                        Text(app.label.take(1).uppercase(), color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (settings.notificationBadgesEnabled && badgeCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .defaultMinSize(minWidth = if (settings.showBadgeCount) 18.dp else 12.dp, minHeight = if (settings.showBadgeCount) 18.dp else 12.dp)
                        .clip(CircleShape)
                        .background(SunsetCoral)
                        .border(1.dp, Color.White.copy(alpha = 0.78f), CircleShape)
                        .padding(horizontal = if (settings.showBadgeCount) 5.dp else 0.dp)
                ) {
                    if (settings.showBadgeCount) Text(if (badgeCount > 99) "99+" else badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (finalShowLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.label,
                style = TextStyle(color = IceWhite, fontSize = settings.labelFontSizeSp.sp, fontWeight = FontWeight.Medium, shadow = Shadow(Color.Black.copy(alpha = 0.65f), Offset(0f, 1.5f), 4f)),
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = finalIconSize + 14.dp)
            )
        }
    }
}
