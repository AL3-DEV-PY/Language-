package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LinguaXPrimary,
    onPrimary = Color.White,
    primaryContainer = LinguaXPrimaryContainer,
    onPrimaryContainer = LinguaXOnPrimaryContainer,
    secondary = LinguaXSecondary,
    onSecondary = Color.White,
    secondaryContainer = LinguaXSecondaryContainer,
    onSecondaryContainer = LinguaXOnSecondaryContainer,
    background = LinguaXBackground,
    onBackground = LinguaXOnSurface,
    surface = LinguaXSurface,
    onSurface = LinguaXOnSurface,
    surfaceVariant = LinguaXSurfaceVariant,
    tertiary = LinguaXAccentCyan,
    error = LinguaXAccentFlame
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFEEF2FF),
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFF5F3FF),
    background = LinguaXDarkBackground,
    onBackground = LinguaXDarkOnSurface,
    surface = LinguaXDarkSurface,
    onSurface = LinguaXDarkOnSurface,
    surfaceVariant = LinguaXDarkSurfaceVariant,
    tertiary = LinguaXAccentCyan,
    error = LinguaXAccentFlame
)

@Composable
fun LinguaXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
