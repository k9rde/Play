package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SpectrumColorScheme = darkColorScheme(
    primary = PrismCyan,
    onPrimary = ObsidianBg,
    primaryContainer = ObsidianSurfaceElevated,
    onPrimaryContainer = PrismCyan,
    secondary = PrismMagenta,
    onSecondary = ObsidianBg,
    secondaryContainer = ObsidianSurfaceElevated,
    onSecondaryContainer = PrismMagenta,
    tertiary = PrismAmber,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderSubtle,
    error = PrismCoral,
    onError = ObsidianBg
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SpectrumBlocksTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpectrumColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
