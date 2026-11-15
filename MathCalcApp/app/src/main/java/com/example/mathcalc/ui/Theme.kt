package com.example.mathcalc.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ---------- Fintech palette ----------

val Indigo = Color(0xFF7C6FF0)      // vibrant indigo/violet primary
val Violet = Color(0xFF9B8CFF)      // lighter violet for gradients
val Teal = Color(0xFF4DD8C0)        // accent teal (results, success)
val Coral = Color(0xFFFF6B81)       // coral for errors / destructive
val NavyBg = Color(0xFF0E0F1A)      // deep navy background
val NavySurface = Color(0xFF1A1C2B) // elevated surface
val NavySurfaceHigh = Color(0xFF232536)
val NavyField = Color(0xFF141623)
val TextPrimary = Color(0xFFEAEBF5)
val TextMuted = Color(0xFF9BA0B8)
val RootGreen = Teal // kept for compatibility with result cards

// Backwards-compatible aliases
val AccentBlue = Indigo
val AccentPurple = Violet
val AccentLight = Violet
val ErrorSoft = Coral

private val DarkColors = darkColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF37315E),
    onPrimaryContainer = Color(0xFFDCD6FF),
    secondary = Teal,
    onSecondary = Color(0xFF05211D),
    secondaryContainer = Color(0xFF1E3F3A),
    onSecondaryContainer = Color(0xFFC5F2E9),
    tertiary = Violet,
    onTertiary = Color(0xFF1B1140),
    tertiaryContainer = Color(0xFF322A5C),
    onTertiaryContainer = Color(0xFFE4DEFF),
    background = NavyBg,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceHigh,
    onSurfaceVariant = TextMuted,
    surfaceContainer = NavySurface,
    surfaceContainerHigh = NavySurfaceHigh,
    error = Coral,
    onError = Color(0xFF3B0810),
    errorContainer = Color(0xFF541B26),
    onErrorContainer = Color(0xFFFFD4DB),
    outline = Color(0xFF3C3F55),
    outlineVariant = Color(0xFF2B2E42)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B4FD4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E0FF),
    onPrimaryContainer = Color(0xFF1D1254),
    secondary = Color(0xFF159E8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDF2EA),
    onSecondaryContainer = Color(0xFF003B33),
    background = Color(0xFFF7F6FB),
    onBackground = Color(0xFF1A1B2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1B2E),
    surfaceVariant = Color(0xFFE8E6F2),
    onSurfaceVariant = Color(0xFF4B4D60),
    error = Color(0xFFB3261E),
    onError = Color.White,
    outline = Color(0xFF7C7E96)
)

// Rounded corners everywhere (20dp+ for large, 16dp for medium/small elements)
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MathCalcTheme(
    darkTheme: Boolean = true, // dark theme by default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
