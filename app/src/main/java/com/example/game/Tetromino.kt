package com.example.game

data class GridPoint(val col: Int, val row: Int)

enum class TetrominoType(
    val baseHue: Float,
    val rotations: List<List<GridPoint>>
) {
    I(
        baseHue = 190f,
        rotations = listOf(
            listOf(GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1), GridPoint(3, 1)),
            listOf(GridPoint(2, 0), GridPoint(2, 1), GridPoint(2, 2), GridPoint(2, 3)),
            listOf(GridPoint(0, 2), GridPoint(1, 2), GridPoint(2, 2), GridPoint(3, 2)),
            listOf(GridPoint(1, 0), GridPoint(1, 1), GridPoint(1, 2), GridPoint(1, 3))
        )
    ),
    O(
        baseHue = 46f,
        rotations = listOf(
            listOf(GridPoint(1, 0), GridPoint(2, 0), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(1, 0), GridPoint(2, 0), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(1, 0), GridPoint(2, 0), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(1, 0), GridPoint(2, 0), GridPoint(1, 1), GridPoint(2, 1))
        )
    ),
    T(
        baseHue = 285f,
        rotations = listOf(
            listOf(GridPoint(1, 0), GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(1, 0), GridPoint(1, 1), GridPoint(2, 1), GridPoint(1, 2)),
            listOf(GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1), GridPoint(1, 2)),
            listOf(GridPoint(1, 0), GridPoint(0, 1), GridPoint(1, 1), GridPoint(1, 2))
        )
    ),
    S(
        baseHue = 148f,
        rotations = listOf(
            listOf(GridPoint(1, 0), GridPoint(2, 0), GridPoint(0, 1), GridPoint(1, 1)),
            listOf(GridPoint(1, 0), GridPoint(1, 1), GridPoint(2, 1), GridPoint(2, 2)),
            listOf(GridPoint(1, 1), GridPoint(2, 1), GridPoint(0, 2), GridPoint(1, 2)),
            listOf(GridPoint(0, 0), GridPoint(0, 1), GridPoint(1, 1), GridPoint(1, 2))
        )
    ),
    Z(
        baseHue = 350f,
        rotations = listOf(
            listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(2, 0), GridPoint(1, 1), GridPoint(2, 1), GridPoint(1, 2)),
            listOf(GridPoint(0, 1), GridPoint(1, 1), GridPoint(1, 2), GridPoint(2, 2)),
            listOf(GridPoint(1, 0), GridPoint(0, 1), GridPoint(1, 1), GridPoint(0, 2))
        )
    ),
    J(
        baseHue = 224f,
        rotations = listOf(
            listOf(GridPoint(0, 0), GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(1, 0), GridPoint(2, 0), GridPoint(1, 1), GridPoint(1, 2)),
            listOf(GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1), GridPoint(2, 2)),
            listOf(GridPoint(1, 0), GridPoint(1, 1), GridPoint(0, 2), GridPoint(1, 2))
        )
    ),
    L(
        baseHue = 26f,
        rotations = listOf(
            listOf(GridPoint(2, 0), GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1)),
            listOf(GridPoint(1, 0), GridPoint(1, 1), GridPoint(1, 2), GridPoint(2, 2)),
            listOf(GridPoint(0, 1), GridPoint(1, 1), GridPoint(2, 1), GridPoint(0, 2)),
            listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(1, 1), GridPoint(1, 2))
        )
    );

    fun cells(rotation: Int): List<GridPoint> {
        val normalized = ((rotation % 4) + 4) % 4
        return rotations[normalized]
    }
}

object SrsWallKicks {
    // Standard SRS kick tests (dx, dy) with +y pointing downward on screen.
    private val jlstzCw = mapOf(
        0 to listOf(GridPoint(0, 0), GridPoint(-1, 0), GridPoint(-1, -1), GridPoint(0, 2), GridPoint(-1, 2)),
        1 to listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(1, 1), GridPoint(0, -2), GridPoint(1, -2)),
        2 to listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(1, -1), GridPoint(0, 2), GridPoint(1, 2)),
        3 to listOf(GridPoint(0, 0), GridPoint(-1, 0), GridPoint(-1, 1), GridPoint(0, -2), GridPoint(-1, -2))
    )

    private val jlstzCcw = mapOf(
        0 to listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(1, -1), GridPoint(0, 2), GridPoint(1, 2)),
        1 to listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(1, 1), GridPoint(0, -2), GridPoint(1, -2)),
        2 to listOf(GridPoint(0, 0), GridPoint(-1, 0), GridPoint(-1, -1), GridPoint(0, 2), GridPoint(-1, 2)),
        3 to listOf(GridPoint(0, 0), GridPoint(-1, 0), GridPoint(-1, 1), GridPoint(0, -2), GridPoint(-1, -2))
    )

    private val iCw = mapOf(
        0 to listOf(GridPoint(0, 0), GridPoint(-2, 0), GridPoint(1, 0), GridPoint(-2, 1), GridPoint(1, -2)),
        1 to listOf(GridPoint(0, 0), GridPoint(-1, 0), GridPoint(2, 0), GridPoint(-1, -2), GridPoint(2, 1)),
        2 to listOf(GridPoint(0, 0), GridPoint(2, 0), GridPoint(-1, 0), GridPoint(2, -1), GridPoint(-1, 2)),
        3 to listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(-2, 0), GridPoint(1, 2), GridPoint(-2, -1))
    )

    private val iCcw = mapOf(
        0 to listOf(GridPoint(0, 0), GridPoint(-1, 0), GridPoint(2, 0), GridPoint(-1, -2), GridPoint(2, 1)),
        1 to listOf(GridPoint(0, 0), GridPoint(2, 0), GridPoint(-1, 0), GridPoint(2, -1), GridPoint(-1, 2)),
        2 to listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(-2, 0), GridPoint(1, 2), GridPoint(-2, -1)),
        3 to listOf(GridPoint(0, 0), GridPoint(-2, 0), GridPoint(1, 0), GridPoint(-2, 1), GridPoint(1, -2))
    )

    fun getKicks(type: TetrominoType, fromRotation: Int, clockwise: Boolean): List<GridPoint> {
        val norm = ((fromRotation % 4) + 4) % 4
        return when {
            type == TetrominoType.O -> listOf(GridPoint(0, 0))
            type == TetrominoType.I -> if (clockwise) iCw[norm]!! else iCcw[norm]!!
            else -> if (clockwise) jlstzCw[norm]!! else jlstzCcw[norm]!!
        }
    }
}

enum class ColorShiftMode {
    CONTINUOUS_SPECTRUM,
    LEVEL_GRADIENT,
    AURORA_PASTEL
}

enum class ColorShiftSpeed(val degreesPerSecond: Float) {
    SLOW(18f),
    MEDIUM(44f),
    FAST(92f)
}
