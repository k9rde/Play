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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.game.BOARD_COLS
import com.example.game.BOARD_ROWS
import com.example.game.ClearBanner
import com.example.game.GameUiState
import com.example.game.SpectrumColorEngine
import com.example.game.TilePalette
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianBgSecondary
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.PrismAmber
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismMagenta
import kotlinx.coroutines.delay
import kotlin.math.abs

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
    val boardShape = RoundedCornerShape(18.dp)
    var visibleBanner by remember { mutableStateOf<ClearBanner?>(null) }

    LaunchedEffect(uiState.clearBanner?.id) {
        val banner = uiState.clearBanner
        if (banner != null) {
            visibleBanner = banner
            delay(950L)
            visibleBanner = null
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(BOARD_COLS.toFloat() / BOARD_ROWS.toFloat())
            .clip(boardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E1E),
                        ObsidianBgSecondary,
                        Color(0xFF070913)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        PrismCyan.copy(alpha = 0.55f),
                        ObsidianBorder,
                        PrismMagenta.copy(alpha = 0.55f)
                    )
                ),
                shape = boardShape
            )
            .then(
                if (gesturesEnabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onTapRotate() }
                            )
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

                                    // Fast downward flick triggers hard drop
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

            // Subtle dynamic ambient aura behind the grid matching current spectrum phase
            val ambientColor = SpectrumColorEngine.accentColorForPhase(phase, mode, level)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ambientColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.35f),
                    radius = size.maxDimension * 0.75f
                )
            )

            // Crisp hairline grid lines
            val gridLineColor = Color.White.copy(alpha = 0.045f)
            for (c in 1 until BOARD_COLS) {
                val x = c * cellW
                drawLine(
                    color = gridLineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            for (r in 1 until BOARD_ROWS) {
                val y = r * cellH
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Locked cells with live gradual color shift
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
                        drawClearingBlock(
                            col = c,
                            row = r,
                            cellW = cellW,
                            cellH = cellH,
                            glowColor = palette.glow
                        )
                    } else {
                        drawGlossyBlock(
                            col = c,
                            row = r,
                            cellW = cellW,
                            cellH = cellH,
                            palette = palette
                        )
                    }
                }
            }

            val active = uiState.activePiece
            if (active != null && uiState.clearingRows.isEmpty()) {
                val activeSamplePalette = SpectrumColorEngine.paletteForBlock(
                    baseHue = active.type.baseHue,
                    hueOffset = active.hueOffset,
                    colorPhase = phase,
                    row = active.y.coerceAtLeast(0),
                    col = active.x.coerceIn(0, BOARD_COLS - 1),
                    level = level,
                    mode = mode,
                    isLocked = false
                )

                // Ghost piece preview
                val ghostY = uiState.ghostY
                if (ghostEnabled && ghostY != null && ghostY >= active.y) {
                    for (local in active.type.cells(active.rotation)) {
                        val gc = active.x + local.col
                        val gr = ghostY + local.row
                        if (gr in 0 until BOARD_ROWS && gc in 0 until BOARD_COLS) {
                            drawGhostBlock(
                                col = gc,
                                row = gr,
                                cellW = cellW,
                                cellH = cellH,
                                accent = activeSamplePalette.glow
                            )
                        }
                    }
                }

                // Active falling piece (each block gets slight positional ripple + continuous phase shift)
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
                        drawGlossyBlock(
                            col = cell.col,
                            row = cell.row,
                            cellW = cellW,
                            cellH = cellH,
                            palette = cellPalette,
                            isActive = true
                        )
                    }
                }
            }
        }

        // Line-clear floating celebration banner
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xD90E1326))
                        .border(
                            width = 1.dp,
                            color = if (banner.linesCleared >= 4) PrismAmber else PrismCyan,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (banner.linesCleared >= 4) PrismAmber else PrismCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${banner.points}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    if (banner.combo > 1) {
                        Text(
                            text = stringResource(R.string.combo_format, banner.combo),
                            style = MaterialTheme.typography.labelMedium,
                            color = PrismMagenta
                        )
                    }
                }
            }
        }
    }
}

fun DrawScope.drawGlossyBlock(
    col: Int,
    row: Int,
    cellW: Float,
    cellH: Float,
    palette: TilePalette,
    isActive: Boolean = false
) {
    val gap = (cellW * 0.06f).coerceIn(1.2f, 2.6f)
    val left = col * cellW + gap
    val top = row * cellH + gap
    val w = (cellW - gap * 2f).coerceAtLeast(2f)
    val h = (cellH - gap * 2f).coerceAtLeast(2f)
    val radius = (w * 0.22f).coerceIn(3f, 8f)

    if (isActive) {
        drawRoundRect(
            color = palette.glow.copy(alpha = 0.25f),
            topLeft = Offset(left - gap, top - gap),
            size = Size(w + gap * 2f, h + gap * 2f),
            cornerRadius = CornerRadius(radius + 2f, radius + 2f)
        )
    }

    // Main block body gradient
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

    // Top-left bevel sheen
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
    drawPath(
        path = sheenPath,
        color = Color.White.copy(alpha = 0.24f)
    )

    // Subtle crisp inner border
    drawRoundRect(
        color = Color.White.copy(alpha = 0.16f),
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
    val gap = (cellW * 0.08f).coerceIn(1.5f, 3f)
    val left = col * cellW + gap
    val top = row * cellH + gap
    val w = (cellW - gap * 2f).coerceAtLeast(2f)
    val h = (cellH - gap * 2f).coerceAtLeast(2f)
    val radius = (w * 0.22f).coerceIn(3f, 8f)

    drawRoundRect(
        color = accent.copy(alpha = 0.08f),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.52f),
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
