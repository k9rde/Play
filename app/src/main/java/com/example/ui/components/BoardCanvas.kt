package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.AppVisualTheme
import com.example.game.BOARD_COLS
import com.example.game.BOARD_ROWS
import com.example.game.ClearBanner
import com.example.game.GameUiState
import com.example.game.SpectrumColorEngine
import com.example.game.TilePalette
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGamePalette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private data class BurstParticle(
    var xNorm: Float,
    var yNorm: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    var life: Float = 1f,
    val radiusDp: Float
)

@Composable
fun BoardCanvas(
    uiState: GameUiState,
    colorPhaseProvider: () -> Float,
    gesturesEnabled: Boolean,
    ghostEnabled: Boolean,
    onTapRotate: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gamePalette = LocalGamePalette.current
    val visualTheme = uiState.statsAndSettings.visualTheme
    val boardShape = RoundedCornerShape(22.dp)
    var visibleBanner by remember { mutableStateOf<ClearBanner?>(null) }
    val particles = remember { mutableStateListOf<BurstParticle>() }

    // Spawn luminous spectrum particles whenever rows start clearing
    LaunchedEffect(uiState.clearingRows) {
        if (uiState.clearingRows.isNotEmpty()) {
            val phase = colorPhaseProvider()
            val mode = uiState.statsAndSettings.colorShiftMode
            for (row in uiState.clearingRows) {
                for (c in 0 until BOARD_COLS) {
                    val pal = SpectrumColorEngine.paletteForBlock(
                        baseHue = (c * 36f),
                        hueOffset = 0f,
                        colorPhase = phase,
                        row = row,
                        col = c,
                        level = uiState.level,
                        mode = mode,
                        isLocked = false
                    )
                    repeat(3) {
                        particles.add(
                            BurstParticle(
                                xNorm = (c + 0.5f) / BOARD_COLS.toFloat(),
                                yNorm = (row + 0.5f) / BOARD_ROWS.toFloat(),
                                vx = (Random.nextFloat() - 0.5f) * 0.9f,
                                vy = (Random.nextFloat() - 0.7f) * 0.85f,
                                color = pal.glow,
                                life = 1f,
                                radiusDp = 2.5f + Random.nextFloat() * 3.5f
                            )
                        )
                    }
                }
            }
        }
    }

    // Update particle physics smoothly
    LaunchedEffect(particles.isNotEmpty()) {
        if (particles.isNotEmpty()) {
            var lastNanos = 0L
            while (particles.isNotEmpty()) {
                withFrameNanos { now ->
                    if (lastNanos != 0L) {
                        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        val iter = particles.listIterator()
                        while (iter.hasNext()) {
                            val p = iter.next()
                            p.xNorm += p.vx * dt
                            p.yNorm += p.vy * dt
                            p.life -= dt * 1.9f
                            if (p.life <= 0f) {
                                iter.remove()
                            }
                        }
                    }
                    lastNanos = now
                }
            }
        }
    }

    LaunchedEffect(uiState.clearBanner?.id) {
        val banner = uiState.clearBanner
        if (banner != null) {
            visibleBanner = banner
            delay(900L)
            visibleBanner = null
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(BOARD_COLS.toFloat() / BOARD_ROWS.toFloat())
            .shadow(
                elevation = if (gamePalette.isLight) 14.dp else 8.dp,
                shape = boardShape,
                ambientColor = gamePalette.accentPrimary.copy(alpha = 0.25f),
                spotColor = gamePalette.accentSecondary.copy(alpha = 0.25f)
            )
            .clip(boardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(gamePalette.boardBgTop, gamePalette.boardBgBottom)
                )
            )
            .border(
                width = if (visualTheme == AppVisualTheme.RETRO_SYNTH) 2.dp else 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        gamePalette.accentPrimary.copy(alpha = 0.7f),
                        gamePalette.border,
                        gamePalette.accentSecondary.copy(alpha = 0.7f)
                    )
                ),
                shape = boardShape
            )
            .then(
                if (gesturesEnabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onTapRotate() })
                        }
                        .pointerInput(Unit) {
                            var accumX = 0f
                            var accumY = 0f
                            var hardDroppedInGesture = false
                            detectDragGestures(
                                onDragStart = {
                                    accumX = 0f
                                    accumY = 0f
                                    hardDroppedInGesture = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (hardDroppedInGesture) return@detectDragGestures

                                    val cellWidth = (size.width / BOARD_COLS.toFloat()).coerceAtLeast(24f)
                                    val cellHeight = (size.height / BOARD_ROWS.toFloat()).coerceAtLeast(24f)
                                    val stepX = cellWidth * 0.85f
                                    val stepY = cellHeight * 0.72f

                                    accumX += dragAmount.x
                                    accumY += dragAmount.y

                                    while (accumX >= stepX) {
                                        onMoveRight()
                                        accumX -= stepX
                                    }
                                    while (accumX <= -stepX) {
                                        onMoveLeft()
                                        accumX += stepX
                                    }

                                    if (dragAmount.y > cellHeight * 1.35f && abs(dragAmount.y) > abs(dragAmount.x) * 1.5f) {
                                        onHardDrop()
                                        hardDroppedInGesture = true
                                        return@detectDragGestures
                                    }

                                    while (accumY >= stepY) {
                                        onSoftDrop()
                                        accumY -= stepY
                                    }
                                }
                            )
                        }
                } else {
                    Modifier
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val phase = colorPhaseProvider()
            val mode = uiState.statsAndSettings.colorShiftMode
            val level = uiState.level

            val cellW = size.width / BOARD_COLS
            val cellH = size.height / BOARD_ROWS

            val ambientColor = SpectrumColorEngine.accentColorForPhase(phase, mode, level)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ambientColor.copy(alpha = 0.11f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.30f),
                    radius = size.maxDimension * 0.78f
                )
            )

            // Active piece vertical alignment lane highlight
            val active = uiState.activePiece
            if (active != null && uiState.clearingRows.isEmpty()) {
                val occupiedCols = active.worldCells().map { it.col }.filter { it in 0 until BOARD_COLS }.toSet()
                for (c in occupiedCols) {
                    drawRect(
                        color = ambientColor.copy(alpha = 0.045f),
                        topLeft = Offset(c * cellW, 0f),
                        size = Size(cellW, size.height)
                    )
                }
            }

            // Crisp hairline grid
            for (c in 1 until BOARD_COLS) {
                val x = c * cellW
                drawLine(
                    color = gamePalette.gridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            for (r in 1 until BOARD_ROWS) {
                val y = r * cellH
                drawLine(
                    color = gamePalette.gridLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Locked cells
            for (r in 0 until BOARD_ROWS) {
                val isClearing = r in uiState.clearingRows
                for (c in 0 until BOARD_COLS) {
                    val locked = uiState.board[r][c] ?: continue
                    val palette = SpectrumColorEngine.paletteForBlock(
                        baseHue = locked.type.baseHue,
                        hueOffset = locked.hueOffset,
                        colorPhase = phase,
                        row = r,
                        col = c,
                        level = level,
                        mode = mode,
                        isLocked = true
                    )
                    if (isClearing) {
                        drawClearingBlock(c, r, cellW, cellH, palette.glow)
                    } else {
                        drawStyledBlock(
                            col = c,
                            row = r,
                            cellW = cellW,
                            cellH = cellH,
                            palette = palette,
                            visualTheme = visualTheme,
                            isActive = false
                        )
                    }
                }
            }

            // Ghost + active falling piece
            if (active != null && uiState.clearingRows.isEmpty()) {
                val samplePalette = SpectrumColorEngine.paletteForBlock(
                    baseHue = active.type.baseHue,
                    hueOffset = active.hueOffset,
                    colorPhase = phase,
                    row = active.y.coerceAtLeast(0),
                    col = active.x.coerceIn(0, BOARD_COLS - 1),
                    level = level,
                    mode = mode,
                    isLocked = false
                )

                val ghostY = uiState.ghostY
                if (ghostEnabled && ghostY != null && ghostY >= active.y) {
                    for (local in active.type.cells(active.rotation)) {
                        val gc = active.x + local.col
                        val gr = ghostY + local.row
                        if (gr in 0 until BOARD_ROWS && gc in 0 until BOARD_COLS) {
                            drawGhostBlock(gc, gr, cellW, cellH, samplePalette.glow)
                        }
                    }
                }

                for (cell in active.worldCells()) {
                    if (cell.row in 0 until BOARD_ROWS && cell.col in 0 until BOARD_COLS) {
                        val cellPalette = SpectrumColorEngine.paletteForBlock(
                            baseHue = active.type.baseHue,
                            hueOffset = active.hueOffset,
                            colorPhase = phase,
                            row = cell.row,
                            col = cell.col,
                            level = level,
                            mode = mode,
                            isLocked = false
                        )
                        drawStyledBlock(
                            col = cell.col,
                            row = cell.row,
                            cellW = cellW,
                            cellH = cellH,
                            palette = cellPalette,
                            visualTheme = visualTheme,
                            isActive = true
                        )
                    }
                }
            }

            // Particle bursts
            for (p in particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.radiusDp.dp.toPx() * p.life.coerceIn(0.3f, 1f),
                    center = Offset(p.xNorm * size.width, p.yNorm * size.height)
                )
            }
        }

        AnimatedVisibility(
            visible = visibleBanner != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 1.08f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
        ) {
            visibleBanner?.let { banner ->
                val title = when (banner.linesCleared) {
                    1 -> stringResource(R.string.clear_single)
                    2 -> stringResource(R.string.clear_double)
                    3 -> stringResource(R.string.clear_triple)
                    else -> stringResource(R.string.clear_tetris)
                }
                val accent = if (banner.linesCleared >= 4) gamePalette.accentTertiary else gamePalette.accentPrimary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xE0101424))
                        .border(1.5.dp, accent, RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${banner.points}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    if (banner.combo > 1) {
                        Text(
                            text = stringResource(R.string.combo_format, banner.combo),
                            style = MaterialTheme.typography.labelMedium,
                            color = gamePalette.accentSecondary
                        )
                    }
                }
            }
        }
    }
}

fun DrawScope.drawStyledBlock(
    col: Int,
    row: Int,
    cellW: Float,
    cellH: Float,
    palette: TilePalette,
    visualTheme: AppVisualTheme,
    isActive: Boolean = false
) {
    val gap = (cellW * 0.065f).coerceIn(1.2f, 2.8f)
    val left = col * cellW + gap
    val top = row * cellH + gap
    val w = (cellW - gap * 2f).coerceAtLeast(2f)
    val h = (cellH - gap * 2f).coerceAtLeast(2f)
    val radius = when (visualTheme) {
        AppVisualTheme.RETRO_SYNTH -> (w * 0.12f).coerceIn(2f, 4f)
        AppVisualTheme.DAYLIGHT_CERAMIC -> (w * 0.28f).coerceIn(4f, 9f)
        AppVisualTheme.MIDNIGHT_GLASS -> (w * 0.24f).coerceIn(3f, 8f)
    }

    if (isActive) {
        drawRoundRect(
            color = palette.glow.copy(alpha = 0.28f),
            topLeft = Offset(left - gap, top - gap),
            size = Size(w + gap * 2f, h + gap * 2f),
            cornerRadius = CornerRadius(radius + 2f, radius + 2f)
        )
    }

    if (visualTheme == AppVisualTheme.RETRO_SYNTH) {
        drawRoundRect(
            color = palette.body.copy(alpha = 0.28f),
            topLeft = Offset(left, top),
            size = Size(w, h),
            cornerRadius = CornerRadius(radius, radius)
        )
        drawRoundRect(
            color = palette.highlight,
            topLeft = Offset(left, top),
            size = Size(w, h),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = (w * 0.11f).coerceIn(1.8f, 3.2f))
        )
        drawRoundRect(
            color = palette.glow.copy(alpha = 0.7f),
            topLeft = Offset(left + w * 0.28f, top + h * 0.28f),
            size = Size(w * 0.44f, h * 0.44f),
            cornerRadius = CornerRadius(radius * 0.5f, radius * 0.5f)
        )
        return
    }

    // Crystal / Ceramic glossy body
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(palette.highlight, palette.body, palette.shadow),
            start = Offset(left, top),
            end = Offset(left + w, top + h)
        ),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Soft glass pill highlight near top edge
    drawRoundRect(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(left + w * 0.16f, top + h * 0.12f),
        size = Size(w * 0.68f, (h * 0.16f).coerceAtLeast(2f)),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Subtle specular corner glint
    val inset = w * 0.14f
    val sheenPath = Path().apply {
        moveTo(left + radius * 0.4f, top + inset)
        lineTo(left + w - inset, top + inset)
        lineTo(left + w - inset * 1.6f, top + inset * 1.9f)
        lineTo(left + inset * 1.9f, top + inset * 1.9f)
        lineTo(left + inset * 1.9f, top + h - inset * 1.6f)
        lineTo(left + inset, top + h - radius * 0.4f)
        close()
    }
    drawPath(path = sheenPath, color = Color.White.copy(alpha = 0.14f))

    drawRoundRect(
        color = Color.White.copy(alpha = 0.20f),
        topLeft = Offset(left + 0.5f, top + 0.5f),
        size = Size(w - 1f, h - 1f),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawGhostBlock(
    col: Int,
    row: Int,
    cellW: Float,
    cellH: Float,
    accent: Color
) {
    val gap = (cellW * 0.09f).coerceIn(1.5f, 3f)
    val left = col * cellW + gap
    val top = row * cellH + gap
    val w = (cellW - gap * 2f).coerceAtLeast(2f)
    val h = (cellH - gap * 2f).coerceAtLeast(2f)
    val radius = (w * 0.24f).coerceIn(3f, 8f)

    drawRoundRect(
        color = accent.copy(alpha = 0.10f),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.58f),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1.6f)
    )
}

private fun DrawScope.drawClearingBlock(
    col: Int,
    row: Int,
    cellW: Float,
    cellH: Float,
    glowColor: Color
) {
    val left = col * cellW
    val top = row * cellH
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, glowColor, Color.White)
        ),
        topLeft = Offset(left, top),
        size = Size(cellW, cellH)
    )
}
