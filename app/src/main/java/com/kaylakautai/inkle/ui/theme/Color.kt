package com.kaylakautai.inkle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ===============================================================
// Fixed palette: the same in light and dark theme.
// ===============================================================

val Yellow = Color(0xFFf9c859)
val Blue = Color(0xFF6698CC)
val Green = Color(0xFF62823f)
val Purple = Color(0xFFD9ADD1)
val Orange = Color(0xFFF08C21)
val LightPink = Color(0xFFfff0f5)

val allColors = listOf(Orange, Purple, Green, Blue, Yellow)

/**
 * Neutral base colours. Use these directly only where a colour must NOT change with the theme,
 * e.g. dark text on a coloured book spine or on the yellow stamp card. Everywhere else, use the
 * theme-aware tokens below.
 */
val InkBase = Color(0xFF1C1917)
val PaperBase = Color(0xFFF7F4EF)
val MutedBase = Color(0xFF8A8175)
val CoverBase = Color(0xFFE7E1D6)

// Dark theme neutrals: warm, slightly brown blacks so it still feels like paper and ink.
val NightBase = Color(0xFF171412)
val NightCard = Color(0xFF26221F)
val NightMuted = Color(0xFFA59D92)
val NightCover = Color(0xFF3B3532)

// ===============================================================
// Theme-aware tokens: follow light/dark via MaterialTheme (see Theme.kt).
// Only usable from @Composable code.
// ===============================================================

/** Main text and icons; also the fill of dark buttons and selected chips. */
val Ink: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground

/** Text and icons drawn on an [Ink] fill (e.g. a selected chip or the Save button). */
val OnInk: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

/** Page background. */
val Paper: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

/** Kept for older screens; same as [Paper]. */
val Beige: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

/** Cards, dialogs, text fields and unselected chips (white in light theme). */
val CardBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

/** Secondary text: labels, hints, metadata. */
val Muted: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

/** Borders and dividers. */
val Cover: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

/** Dashboard bottom bar background (dark in both themes). */
val NavBar: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.inverseSurface
