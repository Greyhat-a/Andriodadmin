package com.nexus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Accent colours that Material 3 has no slot for. Meanings are fixed app-wide:
 * green = success / operational, yellow = warning, orange = secondary highlight.
 * Blue (primary) and red (error) come from the Material colour scheme.
 */
@Immutable
data class NexusExtraColors(
    val success: Color,
    val warning: Color,
    val orange: Color,
)

private val LightExtras = NexusExtraColors(
    success = LightGreen,
    warning = LightYellow,
    orange = LightOrange,
)

private val DarkExtras = NexusExtraColors(
    success = DarkGreen,
    warning = DarkYellow,
    orange = DarkOrange,
)

private val LocalNexusExtras = staticCompositionLocalOf { LightExtras }

object NexusExtras {
    val colors: NexusExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNexusExtras.current
}

private val LightScheme = lightColorScheme(
    primary = LightBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlueContainer,
    onPrimaryContainer = LightText,
    secondary = LightOrange,
    onSecondary = Color.White,
    secondaryContainer = LightOrangeContainer,
    onSecondaryContainer = LightText,
    tertiary = LightGreen,
    onTertiary = Color.White,
    error = LightRed,
    onError = Color.White,
    errorContainer = LightRedContainer,
    onErrorContainer = LightText,
    background = LightBackground,
    onBackground = LightText,
    surface = LightBackground,
    onSurface = LightText,
    surfaceVariant = LightSecondaryBackground,
    onSurfaceVariant = LightSecondaryText,
    surfaceTint = LightBlue,
    outline = LightSecondaryText,
    outlineVariant = LightBorder,
    surfaceContainerLowest = LightBackground,
    surfaceContainerLow = LightBackground,
    surfaceContainer = LightBackground,
    surfaceContainerHigh = LightSecondaryBackground,
    surfaceContainerHighest = LightSecondaryBackground,
)

private val DarkScheme = darkColorScheme(
    primary = DarkBlue,
    onPrimary = Color(0xFF0B2A5B),
    primaryContainer = DarkBlueContainer,
    onPrimaryContainer = DarkText,
    secondary = DarkOrange,
    onSecondary = Color(0xFF3A2100),
    secondaryContainer = DarkOrangeContainer,
    onSecondaryContainer = DarkText,
    tertiary = DarkGreen,
    onTertiary = Color(0xFF0B3A1A),
    error = DarkRed,
    onError = Color(0xFF410E0B),
    errorContainer = DarkRedContainer,
    onErrorContainer = DarkText,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkBackground,
    onSurface = DarkText,
    surfaceVariant = DarkSecondaryBackground,
    onSurfaceVariant = DarkSecondaryText,
    surfaceTint = DarkBlue,
    outline = DarkSecondaryText,
    outlineVariant = DarkBorder,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkBackground,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkCard,
    surfaceContainerHighest = DarkSecondaryBackground,
)

private val NexusTypography = Typography()

/**
 * App theme. Follows the Android system appearance: when the system switches
 * between light and dark, the activity is recreated by Android and this
 * function picks the matching scheme. Dynamic (wallpaper) colour is
 * intentionally not used so the NEXUS palette stays consistent.
 */
@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val extras = if (darkTheme) DarkExtras else LightExtras
    CompositionLocalProvider(LocalNexusExtras provides extras) {
        MaterialTheme(
            colorScheme = scheme,
            typography = NexusTypography,
            content = content,
        )
    }
}
