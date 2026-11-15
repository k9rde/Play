package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ColorShiftMode
import com.example.game.GameUiState
import com.example.game.SpectrumColorEngine
import com.example.game.TetrominoType
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGamePalette

/**
 * Symmetric top HUD strip positioned above the playfield so the 10x20 game board
 * keeps full centered proportions without being squeezed by a side column.
 */
@Composable
fun CompactGameHudStrip(
    uiState: GameUiState,
    colorPhaseProvider: () -> Float,
    onHoldClick: () -> Unit,
    onCycleColorMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    val mode = uiState.statsAndSettings.colorShiftMode
    val holdDesc = stringResource(R.string.cd_hold_piece)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hold piece capsule (tappable)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .shadow(if (palette.isLight) 3.dp else 0.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                .clickable(onClick = onHoldClick)
                .semantics { contentDescription = holdDesc }
                .testTag("hold_piece_button")
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .alpha(if (uiState.canHold) 1f else 0.45f)
        ) {
            Text(
                text = stringResource(R.string.hold_label),
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary
            )
            MiniPiecePreview(
                type = uiState.holdPiece,
                hueOffset = uiState.holdHueOffset,
                colorPhaseProvider = colorPhaseProvider,
                level = uiState.level,
                mode = mode,
                modifier = Modifier.size(38.dp)
            )
        }

        // Center Score / Level / Lines compact telemetry card (tappable to cycle color mode)
        Column(
            modifier = Modifier
                .weight(1f)
                .shadow(if (palette.isLight) 3.dp else 0.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                .clickable(onClick = onCycleColorMode)
                .testTag("cycle_color_mode_button")
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudMetricCell(
                    label = stringResource(R.string.score_label),
                    value = uiState.score.toString(),
                    accent = palette.accentPrimary
                )
                HudMetricCell(
                    label = stringResource(R.string.level_label),
                    value = uiState.level.toString(),
                    accent = palette.accentSecondary
                )
                HudMetricCell(
                    label = stringResource(R.string.lines_label),
                    value = uiState.lines.toString(),
                    accent = palette.accentTertiary
                )
                HudMetricCell(
                    label = stringResource(R.string.best_label),
                    value = uiState.highScore.toString(),
                    accent = palette.textPrimary
                )
            }
            LiveSpectrumBar(
                colorPhaseProvider = colorPhaseProvider,
                mode = mode,
                level = uiState.level,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        // Next queue capsule (next + 2 preview)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .shadow(if (palette.isLight) 3.dp else 0.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.next_label),
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.nextQueue.take(2).forEachIndexed { idx, pair ->
                    MiniPiecePreview(
                        type = pair.first,
                        hueOffset = pair.second,
                        colorPhaseProvider = colorPhaseProvider,
                        level = uiState.level,
                        mode = mode,
                        modifier = Modifier.size(if (idx == 0) 38.dp else 30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HudMetricCell(
    label: String,
    value: String,
    accent: Color
) {
    val palette = LocalGamePalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = accent
        )
    }
}

@Composable
fun MiniPiecePreview(
    type: TetrominoType?,
    hueOffset: Float,
    colorPhaseProvider: () -> Float,
    level: Int,
    mode: ColorShiftMode,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (type != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cells = type.cells(0)
                val minCol = cells.minOf { it.col }
                val maxCol = cells.maxOf { it.col }
                val minRow = cells.minOf { it.row }
                val maxRow = cells.maxOf { it.row }

                val spanCols = (maxCol - minCol + 1).coerceAtLeast(1)
                val spanRows = (maxRow - minRow + 1).coerceAtLeast(1)
                val cellSize = minOf(size.width / 4f, size.height / 3f).coerceAtLeast(5f)
                val offsetX = (size.width - spanCols * cellSize) / 2f
                val offsetY = (size.height - spanRows * cellSize) / 2f
                val phase = colorPhaseProvider()

                for (pt in cells) {
                    val normCol = pt.col - minCol
                    val normRow = pt.row - minRow
                    val pal = SpectrumColorEngine.paletteForBlock(
                        baseHue = type.baseHue,
                        hueOffset = hueOffset,
                        colorPhase = phase,
                        row = normRow + 2,
                        col = normCol,
                        level = level,
                        mode = mode,
                        isLocked = false
                    )
                    val gap = (cellSize * 0.08f).coerceIn(1f, 2f)
                    val s = (cellSize - gap * 2f).coerceAtLeast(2f)
                    val radius = (s * 0.25f).coerceIn(2f, 5f)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(pal.highlight, pal.body),
                            start = Offset(offsetX + normCol * cellSize + gap, offsetY + normRow * cellSize + gap),
                            end = Offset(offsetX + normCol * cellSize + gap + s, offsetY + normRow * cellSize + gap + s)
                        ),
                        topLeft = Offset(offsetX + normCol * cellSize + gap, offsetY + normRow * cellSize + gap),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }
        }
    }
}

@Composable
fun LiveSpectrumBar(
    colorPhaseProvider: () -> Float,
    mode: ColorShiftMode,
    level: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val phase = colorPhaseProvider()
        val c1 = SpectrumColorEngine.paletteForBlock(180f, 0f, phase, 0, 0, level, mode, false).body
        val c2 = SpectrumColorEngine.paletteForBlock(260f, 30f, phase, 4, 3, level, mode, false).body
        val c3 = SpectrumColorEngine.paletteForBlock(340f, 60f, phase, 8, 6, level, mode, false).body
        drawRect(brush = Brush.horizontalGradient(colors = listOf(c1, c2, c3)))
    }
}
