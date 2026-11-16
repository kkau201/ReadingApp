package com.example.readingapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/*
 * Role mapping used by the tokens in Color.kt:
 *   background / onBackground  -> Paper / Ink (page and main text)
 *   primary / onPrimary        -> Ink fill / OnInk (dark buttons, selected chips)
 *   surface / onSurface        -> Card (cards, dialogs, fields)
 *   onSurfaceVariant           -> Muted (secondary text)
 *   outline / outlineVariant   -> Cover (borders, dividers)
 *   inverseSurface             -> NavBar (bottom bar)
 */

private val LightColorScheme = lightColorScheme(
    primary = InkBase,
    onPrimary = Color.White,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = Yellow,
    onTertiary = InkBase,
    background = PaperBase,
    onBackground = InkBase,
    surface = Color.White,
    onSurface = InkBase,
    surfaceVariant = CoverBase,
    onSurfaceVariant = MutedBase,
    outline = CoverBase,
    outlineVariant = CoverBase,
    inverseSurface = InkBase,
    inverseOnSurface = PaperBase
)

private val DarkColorScheme = darkColorScheme(
    primary = PaperBase,
    onPrimary = NightBase,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = Yellow,
    onTertiary = InkBase,
    background = NightBase,
    onBackground = PaperBase,
    surface = NightCard,
    onSurface = PaperBase,
    surfaceVariant = NightCover,
    onSurfaceVariant = NightMuted,
    outline = NightCover,
    outlineVariant = NightCover,
    inverseSurface = NightCard,
    inverseOnSurface = PaperBase
)

private val LocalTypography = staticCompositionLocalOf { Typography }
private val LocalSpacing = staticCompositionLocalOf { Spacing() }

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = LocalTypography.current,
        colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}

/** Whether the app is currently using its dark colour scheme (follows [AppTheme]'s choice). */
@Composable
@ReadOnlyComposable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

object AppTheme {

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}
