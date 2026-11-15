package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.math.sin

data class TilePalette(
    val body: Color,
    val highlight: Color,
    val shadow: Color,
    val glow: Color
)

object SpectrumColorEngine {

    private fun wrapHue(hue: Float): Float {
        val modded = hue % 360f
        return if (modded < 0f) modded + 360f else modded
    }

    /**
     * Computes a luminous, gradually shifting palette for a falling or locked block.
     * Supports three distinct gradual color-shift modes so pieces smoothly evolve over time,
     * altitude/row position, and level progression.
     */
    fun paletteForBlock(
        baseHue: Float,
        hueOffset: Float,
        colorPhase: Float,
        row: Int,
        col: Int,
        level: Int,
        mode: ColorShiftMode,
        isLocked: Boolean
    ): TilePalette {
        val effectiveHue: Float
        val saturation: Float
        val lightness: Float

        when (mode) {
            ColorShiftMode.CONTINUOUS_SPECTRUM -> {
                val spatialRipple = if (isLocked) {
                    (row * 5.5f) + (col * 3.0f)
                } else {
                    (row * 7.5f)
                }
                val timeShift = if (isLocked) colorPhase * 0.45f else colorPhase
                effectiveHue = wrapHue(baseHue + hueOffset + timeShift + spatialRipple)
                saturation = if (isLocked) 0.80f else 0.90f
                lightness = if (isLocked) 0.52f else 0.58f
            }

            ColorShiftMode.LEVEL_GRADIENT -> {
                // Shifts smoothly from cool cyan/blue at level 1 through emerald/amber to magenta/crimson
                val levelShift = ((level - 1) * 34f)
                val descentShift = row * 6.5f
                val pulse = sin(Math.toRadians((colorPhase * 1.4).toDouble())).toFloat() * 18f
                effectiveHue = wrapHue(baseHue * 0.35f + 185f + levelShift + descentShift + pulse + hueOffset * 0.25f)
                saturation = if (isLocked) 0.78f else 0.88f
                lightness = if (isLocked) 0.50f else 0.57f
            }

            ColorShiftMode.AURORA_PASTEL -> {
                // Harmonious sweep across cyan (185), violet (275), rose (335), and mint (160)
                val wave = sin(Math.toRadians((colorPhase + row * 10f + col * 6f + hueOffset).toDouble())).toFloat()
                effectiveHue = wrapHue(245f + wave * 95f + (baseHue - 180f) * 0.22f)
                saturation = if (isLocked) 0.72f else 0.82f
                lightness = if (isLocked) 0.60f else 0.66f
            }
        }

        val body = Color.hsl(effectiveHue, saturation, lightness)
        val highlight = Color.hsl(
            wrapHue(effectiveHue - 8f),
            (saturation - 0.06f).coerceIn(0.4f, 1f),
            (lightness + 0.18f).coerceIn(0.2f, 0.92f)
        )
        val shadow = Color.hsl(
            wrapHue(effectiveHue + 8f),
            (saturation + 0.05f).coerceIn(0.4f, 1f),
            (lightness - 0.20f).coerceIn(0.14f, 0.7f)
        )
        val glow = Color.hsl(
            effectiveHue,
            0.95f,
            (lightness + 0.08f).coerceIn(0.3f, 0.82f)
        )

        return TilePalette(
            body = body,
            highlight = highlight,
            shadow = shadow,
            glow = glow
        )
    }

    fun accentColorForPhase(colorPhase: Float, mode: ColorShiftMode, level: Int): Color {
        return paletteForBlock(
            baseHue = 190f,
            hueOffset = 0f,
            colorPhase = colorPhase,
            row = 6,
            col = 4,
            level = level,
            mode = mode,
            isLocked = false
        ).body
    }
}
