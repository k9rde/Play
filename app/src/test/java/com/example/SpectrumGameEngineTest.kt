package com.example

import com.example.game.ColorShiftMode
import com.example.game.SpectrumColorEngine
import com.example.game.SrsWallKicks
import com.example.game.TetrominoType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpectrumGameEngineTest {

    @Test
    fun allTetrominoRotationsContainFourUniqueCells() {
        for (type in TetrominoType.entries) {
            for (rot in 0 until 4) {
                val cells = type.cells(rot)
                assertEquals("Expected 4 cells for $type rot=$rot", 4, cells.size)
                assertEquals("Expected unique cells for $type rot=$rot", 4, cells.toSet().size)
            }
        }
    }

    @Test
    fun srsWallKicksAlwaysStartWithZeroOffset() {
        for (type in TetrominoType.entries) {
            for (rot in 0 until 4) {
                val cw = SrsWallKicks.getKicks(type, rot, clockwise = true)
                val ccw = SrsWallKicks.getKicks(type, rot, clockwise = false)
                assertTrue(cw.isNotEmpty())
                assertTrue(ccw.isNotEmpty())
                assertEquals(0, cw.first().col)
                assertEquals(0, cw.first().row)
                assertEquals(0, ccw.first().col)
                assertEquals(0, ccw.first().row)
            }
        }
    }

    @Test
    fun spectrumColorEngineShiftsGraduallyOverTime() {
        for (mode in ColorShiftMode.entries) {
            val t0 = SpectrumColorEngine.paletteForBlock(
                baseHue = 190f,
                hueOffset = -45f,
                colorPhase = 0f,
                row = 4,
                col = 3,
                level = 2,
                mode = mode,
                isLocked = false
            )
            val t1 = SpectrumColorEngine.paletteForBlock(
                baseHue = 190f,
                hueOffset = -45f,
                colorPhase = 90f,
                row = 4,
                col = 3,
                level = 2,
                mode = mode,
                isLocked = false
            )
            assertEquals(1f, t0.body.alpha, 0.001f)
            assertEquals(1f, t1.body.alpha, 0.001f)
            assertNotEquals("Color should evolve with phase in $mode", t0.body, t1.body)
        }
    }
}
