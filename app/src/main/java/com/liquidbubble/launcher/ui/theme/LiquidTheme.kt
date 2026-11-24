package com.liquidbubble.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.LauncherThemePreset

val MidnightIndigo = Color(0xFF070913)
val DeepGlass = Color(0xFF141824)
val ElectricCobalt = Color(0xFF2563EB)
val CyberViolet = Color(0xFF7C3AED)
val SunsetCoral = Color(0xFFF43F5E)
val IceWhite = Color(0xFFF8FAFC)
val GlassCyan = Color(0xFFA5B4FC)

@Immutable
data class LiquidColorPalette(
    val preset: LauncherThemePreset,
    val backgroundBase: Color,
    val glassBase: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentWarm: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val rimHighlight: Color,
    val rimCyanTint: Color,
    val isLightGlass: Boolean
)

fun resolveLiquidPalette(settings: LauncherSettingsState): LiquidColorPalette {
    val customAccent = Color(settings.customAccentHex)
    return when (settings.themePreset) {
        LauncherThemePreset.TITANIUM_DARK -> LiquidColorPalette(
            LauncherThemePreset.TITANIUM_DARK, MidnightIndigo, DeepGlass,
            ElectricCobalt, CyberViolet, SunsetCoral, IceWhite, IceWhite.copy(alpha = 0.72f),
            Color.White, GlassCyan, false
        )
        LauncherThemePreset.CRYSTAL_WHITE -> LiquidColorPalette(
            LauncherThemePreset.CRYSTAL_WHITE, Color(0xFF1C2438), Color(0xFFE2E8F0),
            Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFFB7185), IceWhite, IceWhite.copy(alpha = 0.80f),
            Color.White, Color(0xFFBAE6FD), true
        )
        LauncherThemePreset.MIDNIGHT_BLUE -> LiquidColorPalette(
            LauncherThemePreset.MIDNIGHT_BLUE, Color(0xFF040B1A), Color(0xFF0E1B38),
            Color(0xFF38BDF8), ElectricCobalt, Color(0xFF818CF8), IceWhite, Color(0xFFCBD5E1),
            Color.White, Color(0xFF7DD3FC), false
        )
        LauncherThemePreset.AURORA_PURPLE -> LiquidColorPalette(
            LauncherThemePreset.AURORA_PURPLE, Color(0xFF0C061B), Color(0xFF1B1235),
            CyberViolet, Color(0xFFC084FC), Color(0xFF2DD4BF), IceWhite, Color(0xFFE2D9F3),
            Color.White, Color(0xFFD8B4FE), false
        )
        LauncherThemePreset.SUNSET_GLASS -> LiquidColorPalette(
            LauncherThemePreset.SUNSET_GLASS, Color(0xFF140914), Color(0xFF261224),
            SunsetCoral, Color(0xFFFB923C), CyberViolet, IceWhite, Color(0xFFFDE2E8),
            Color.White, Color(0xFFFDA4AF), false
        )
        LauncherThemePreset.OCEAN_GLASS -> LiquidColorPalette(
            LauncherThemePreset.OCEAN_GLASS, Color(0xFF04141D), Color(0xFF0C2431),
            Color(0xFF06B6D4), Color(0xFF2563EB), Color(0xFF34D399), IceWhite, Color(0xFFCFFAFE),
            Color.White, Color(0xFF67E8F9), false
        )
        LauncherThemePreset.CUSTOM -> LiquidColorPalette(
            LauncherThemePreset.CUSTOM, MidnightIndigo, DeepGlass,
            customAccent, CyberViolet, SunsetCoral, IceWhite, IceWhite.copy(alpha = 0.74f),
            Color.White, customAccent.copy(alpha = 0.75f), false
        )
    }
}

val LocalLiquidPalette = staticCompositionLocalOf { resolveLiquidPalette(LauncherSettingsState()) }
val LocalLiquidSettings = staticCompositionLocalOf { LauncherSettingsState() }

private val LiquidTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Thin, fontSize = 54.sp, letterSpacing = (-1.5).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

@Composable
fun LiquidBubbleTheme(
    settings: LauncherSettingsState,
    content: @Composable () -> Unit
) {
    val palette = remember(settings.themePreset, settings.customAccentHex) { resolveLiquidPalette(settings) }
    val colorScheme = remember(palette) {
        darkColorScheme(
            primary = palette.accentPrimary,
            secondary = palette.accentSecondary,
            tertiary = palette.accentWarm,
            background = palette.backgroundBase,
            surface = palette.glassBase,
            onPrimary = IceWhite,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalLiquidPalette provides palette,
        LocalLiquidSettings provides settings
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = LiquidTypography, content = content)
    }
}
