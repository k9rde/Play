package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.game.AppVisualTheme

val LocalGamePalette = staticCompositionLocalOf { paletteForTheme(AppVisualTheme.DAYLIGHT_CERAMIC) }

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SpectrumBlocksTheme(
    visualTheme: AppVisualTheme = AppVisualTheme.DAYLIGHT_CERAMIC,
    content: @Composable () -> Unit
) {
    val palette = paletteForTheme(visualTheme)
    val colorScheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.accentPrimary,
            onPrimary = Color.White,
            secondary = palette.accentSecondary,
            onSecondary = Color.White,
            tertiary = palette.accentTertiary,
            onTertiary = Color.White,
            background = palette.bgTop,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            outlineVariant = palette.borderSubtle,
            error = palette.danger,
            onError = Color.White
        )
    } else {
        darkColorScheme(
            primary = palette.accentPrimary,
            onPrimary = palette.bgBottom,
            secondary = palette.accentSecondary,
            onSecondary = palette.bgBottom,
            tertiary = palette.accentTertiary,
            onTertiary = palette.bgBottom,
            background = palette.bgTop,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            outlineVariant = palette.borderSubtle,
            error = palette.danger,
            onError = palette.bgBottom
        )
    }

    CompositionLocalProvider(LocalGamePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
