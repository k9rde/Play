package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.game.AppVisualTheme

val PrismCyan = Color(0xFF0EA5E9)
val PrismMagenta = Color(0xFFD946EF)
val PrismAmber = Color(0xFFF59E0B)
val PrismMint = Color(0xFF10B981)
val PrismCoral = Color(0xFFF43F5E)

// Legacy aliases kept for compatibility
val ObsidianBg = Color(0xFF070913)
val ObsidianBgSecondary = Color(0xFF0C1021)
val ObsidianSurface = Color(0xFF11162B)
val ObsidianSurfaceElevated = Color(0xFF18203D)
val ObsidianBorder = Color(0xFF28345E)
val ObsidianBorderSubtle = Color(0xFF1D2545)
val TextPrimary = Color(0xFFF3F6FF)
val TextSecondary = Color(0xFF9BA8CC)

@Immutable
data class GameVisualPalette(
    val isLight: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderSubtle: Color,
    val boardBgTop: Color,
    val boardBgBottom: Color,
    val gridLine: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentTertiary: Color,
    val danger: Color
)

fun paletteForTheme(theme: AppVisualTheme): GameVisualPalette = when (theme) {
    AppVisualTheme.DAYLIGHT_CERAMIC -> GameVisualPalette(
        isLight = true,
        bgTop = Color(0xFFF7F5F0),
        bgBottom = Color(0xFFEDE9DF),
        surface = Color(0xFFFFFFFF),
        surfaceElevated = Color(0xFFFDFCF9),
        border = Color(0xFFD9D4C7),
        borderSubtle = Color(0xFFE9E5DB),
        boardBgTop = Color(0xFF1B1E2B),
        boardBgBottom = Color(0xFF11131D),
        gridLine = Color(0x14FFFFFF),
        textPrimary = Color(0xFF171923),
        textSecondary = Color(0xFF5D6275),
        accentPrimary = Color(0xFF0284C7),
        accentSecondary = Color(0xFFC026D3),
        accentTertiary = Color(0xFFD97706),
        danger = Color(0xFFE11D48)
    )

    AppVisualTheme.MIDNIGHT_GLASS -> GameVisualPalette(
        isLight = false,
        bgTop = Color(0xFF0D1120),
        bgBottom = Color(0xFF060811),
        surface = Color(0xFF141A30),
        surfaceElevated = Color(0xFF1D2542),
        border = Color(0xFF2E3A66),
        borderSubtle = Color(0xFF1F2747),
        boardBgTop = Color(0xFF0B0E1B),
        boardBgBottom = Color(0xFF060812),
        gridLine = Color(0x0FFFFFFF),
        textPrimary = Color(0xFFF4F7FF),
        textSecondary = Color(0xFF9AA7CC),
        accentPrimary = Color(0xFF22D3EE),
        accentSecondary = Color(0xFFE879F9),
        accentTertiary = Color(0xFFFBBF24),
        danger = Color(0xFFFB7185)
    )

    AppVisualTheme.RETRO_SYNTH -> GameVisualPalette(
        isLight = false,
        bgTop = Color(0xFF160B29),
        bgBottom = Color(0xFF0A0514),
        surface = Color(0xFF21123B),
        surfaceElevated = Color(0xFF2C184F),
        border = Color(0xFF593196),
        borderSubtle = Color(0xFF392063),
        boardBgTop = Color(0xFF100720),
        boardBgBottom = Color(0xFF080312),
        gridLine = Color(0x1EE879F9),
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFFC4B5FD),
        accentPrimary = Color(0xFF00F5D4),
        accentSecondary = Color(0xFFFF4D8D),
        accentTertiary = Color(0xFFFEE440),
        danger = Color(0xFFFF3366)
    )
}
