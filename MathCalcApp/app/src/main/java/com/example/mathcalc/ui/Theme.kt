package com.example.mathcalc.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Accent palette: deep blue / purple
val AccentBlue = Color(0xFF4F6EF7)
val AccentPurple = Color(0xFF9C6BFF)
val AccentLight = Color(0xFFB9C4FF)
val ErrorSoft = Color(0xFFFF6B6B)
val RootGreen = Color(0xFF4CD98A)

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A3B8F),
    onPrimaryContainer = AccentLight,
    secondary = AccentPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2E7F),
    onSecondaryContainer = Color(0xFFE4D6FF),
    tertiary = AccentLight,
    background = Color(0xFF12131A),
    onBackground = Color(0xFFE7E8EF),
    surface = Color(0xFF1A1B24),
    onSurface = Color(0xFFE7E8EF),
    surfaceVariant = Color(0xFF242633),
    onSurfaceVariant = Color(0xFFBFC2D1),
    error = ErrorSoft,
    onError = Color(0xFF2B0A0A),
    outline = Color(0xFF4A4D62)
)

private val LightColors = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE4FF),
    onPrimaryContainer = Color(0xFF001159),
    secondary = AccentPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADDFF),
    onSecondaryContainer = Color(0xFF25005A),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1A1B24),
    surface = Color.White,
    onSurface = Color(0xFF1A1B24),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MathCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
