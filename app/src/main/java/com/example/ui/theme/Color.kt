package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium 3D Dark Palette for LinguaX
val LinguaXBackground = Color(0xFF0B1020)
val LinguaXSurface = Color(0xFF121A2B)
val LinguaXSurfaceElevated = Color(0xFF182238)
val LinguaXSurfaceHighlight = Color(0xFF1F2C47)
val LinguaXSurfaceGlass = Color(0xCC121A2B)

val LinguaXPrimary = Color(0xFF4F7CFF)
val LinguaXPrimaryLight = Color(0xFF739BFF)
val LinguaXPrimaryDark = Color(0xFF2C5AD6)
val LinguaXPrimaryContainer = Color(0xFF1E2D52)
val LinguaXOnPrimaryContainer = Color(0xFFD6E2FF)

val LinguaXSecondary = Color(0xFF6C5CE7)
val LinguaXSecondaryLight = Color(0xFF8B7EFA)
val LinguaXSecondaryContainer = Color(0xFF282354)
val LinguaXOnSecondaryContainer = Color(0xFFE4E0FF)

val LinguaXAccent = Color(0xFF22D3EE)
val LinguaXAccentLight = Color(0xFF67E8F9)
val LinguaXAccentCyan = Color(0xFF06B6D4)

val LinguaXSuccess = Color(0xFF22C55E)
val LinguaXSuccessLight = Color(0xFF4ADE80)
val LinguaXSuccessGreen = Color(0xFF22C55E)

val LinguaXWarning = Color(0xFFF59E0B)
val LinguaXWarningLight = Color(0xFFFBBF24)
val LinguaXAccentGold = Color(0xFFF59E0B)

val LinguaXError = Color(0xFFEF4444)
val LinguaXErrorLight = Color(0xFFF87171)
val LinguaXAccentFlame = Color(0xFFEF4444)

val LinguaXTextPrimary = Color(0xFFFFFFFF)
val LinguaXTextSecondary = Color(0xFFA8B3C7)
val LinguaXTextTertiary = Color(0xFF64748B)

val LinguaXBorder = Color(0xFF23324D)
val LinguaXBorderLight = Color(0xFF33476E)
val LinguaXBorderGlow = Color(0x664F7CFF)

// Premium 3D Depth Brushes & Gradients
val LinguaXPrimaryGradient = Brush.horizontalGradient(
    listOf(LinguaXPrimary, LinguaXSecondary)
)

val LinguaXAccentGradient = Brush.horizontalGradient(
    listOf(LinguaXAccent, LinguaXPrimary)
)

val LinguaXCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF182238), Color(0xFF121A2B))
)

val LinguaXGlassGradient = Brush.verticalGradient(
    listOf(Color(0xFF1F2C47).copy(alpha = 0.85f), Color(0xFF121A2B).copy(alpha = 0.90f))
)

val LinguaXGoldGradient = Brush.linearGradient(
    listOf(Color(0xFFFFD166), Color(0xFFF59E0B))
)

val LinguaXFlameGradient = Brush.linearGradient(
    listOf(Color(0xFFFF6B6B), Color(0xFFEF4444))
)

val LinguaXGreenGradient = Brush.linearGradient(
    listOf(Color(0xFF4ADE80), Color(0xFF22C55E))
)

val LinguaXBorderGradient = Brush.linearGradient(
    listOf(
        Color(0xFF4F7CFF).copy(alpha = 0.6f),
        Color(0xFF22D3EE).copy(alpha = 0.2f),
        Color(0xFF23324D).copy(alpha = 0.4f)
    )
)
