package dev.xykal.nabungin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.xykal.nabungin.data.prefs.ThemeMode

/**
 * Palet Nabungin: kertas hangat, tinta gelap, satu aksen tumbuh (moss green).
 * Sengaja flat, kontras tinggi, tanpa glow/gradient ungu generik.
 */
@Immutable
data class NabunginColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val onSurface: Color,
    val muted: Color,
    val hairline: Color,
    val accent: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val danger: Color,
    val warning: Color,
    val scrim: Color,
)

private val LightColors = NabunginColors(
    background = Color(0xFFF6F3EE),
    surface = Color(0xFFFCFBF8),
    surfaceAlt = Color(0xFFEFEAE1),
    onSurface = Color(0xFF101418),
    muted = Color(0xFF6E6A63),
    hairline = Color(0xFFE0DAD0),
    accent = Color(0xFF2E6B4F),
    accentInk = Color(0xFFF4FBF7),
    accentSoft = Color(0xFFDCE9E1),
    danger = Color(0xFFB4432F),
    warning = Color(0xFF9A6B12),
    scrim = Color(0x99101418),
)

private val DarkColors = NabunginColors(
    background = Color(0xFF101A17),
    surface = Color(0xFF192721),
    surfaceAlt = Color(0xFF24372E),
    onSurface = Color(0xFFF5F3EB),
    muted = Color(0xFFBAC8BE),
    hairline = Color(0xFF385044),
    accent = Color(0xFF9CDBB3),
    accentInk = Color(0xFF102017),
    accentSoft = Color(0xFF274838),
    danger = Color(0xFFFFA28D),
    warning = Color(0xFFF1C874),
    scrim = Color(0xDD07120D),
)

/** Warna aksen pilihan user untuk tiap tujuan. Sengaja earthy, bukan neon. */
val GoalAccents: List<Color> = listOf(
    Color(0xFF2E6B4F),
    Color(0xFFC4553B),
    Color(0xFF3C5A99),
    Color(0xFFB98A17),
    Color(0xFF7A4A6B),
    Color(0xFF1F7A8C),
)

val LocalNabunginColors = staticCompositionLocalOf { LightColors }

object NabunginSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

private val NabunginTypography: Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.6.sp,
    ),
)

@Composable
fun NabunginTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (dark) DarkColors else LightColors
    val scheme = remember(dark) {
        if (dark) {
            darkColorScheme(
                primary = DarkColors.accent,
                onPrimary = DarkColors.accentInk,
                secondary = DarkColors.surfaceAlt,
                background = DarkColors.background,
                onBackground = DarkColors.onSurface,
                surface = DarkColors.surface,
                onSurface = DarkColors.onSurface,
                outline = DarkColors.hairline,
                error = DarkColors.danger,
            )
        } else {
            lightColorScheme(
                primary = LightColors.accent,
                onPrimary = LightColors.accentInk,
                secondary = LightColors.surfaceAlt,
                background = LightColors.background,
                onBackground = LightColors.onSurface,
                surface = LightColors.surface,
                onSurface = LightColors.onSurface,
                outline = LightColors.hairline,
                error = LightColors.danger,
            )
        }
    }
    CompositionLocalProvider(LocalNabunginColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = NabunginTypography, content = content)
    }
}
