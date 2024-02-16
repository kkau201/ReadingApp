package com.example.readingapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    secondary = Green,
    tertiary = Yellow,
    background = Pink,
    surface = Blue,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.DarkGray,
    onSurface = Color.White
)

private val LocalColors = staticCompositionLocalOf { LightColorScheme }
private val LocalTypography = staticCompositionLocalOf { Typography }
private val LocalSpacing = staticCompositionLocalOf { Spacing() }

object AppTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}