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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ColorShiftMode
import com.example.game.GameUiState
import com.example.game.SpectrumColorEngine
import com.example.game.TetrominoType
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianBorderSubtle
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismAmber
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.TextSecondary

@Composable
fun SideHudColumn(
    uiState: GameUiState,
    colorPhaseProvider: () -> Float,
    onHoldClick: () -> Unit,
    onCycleColorMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = uiState.statsAndSettings.colorShiftMode

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hold piece box (interactive, >= 48dp touch target)
        val holdDesc = stringResource(R.string.cd_hold_piece)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianSurface)
                .border(
                    width = 1.dp,
                    color = if (uiState.canHold) ObsidianBorder else ObsidianBorderSubtle,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onHoldClick)
                .semantics { contentDescription = holdDesc }
                .testTag("hold_piece_button")
                .padding(vertical = 8.dp, horizontal = 6.dp)
                .alpha(if (uiState.canHold) 1f else 0.5f)
        ) {
            Text(
                text = stringResource(R.string.hold_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            MiniPiecePreview(
                type = uiState.holdPiece,
                hueOffset = uiState.holdHueOffset,
                colorPhaseProvider = colorPhaseProvider,
                level = uiState.level,
                mode = mode,
                modifier = Modifier.size(48.dp)
            )
        }

        // Next pieces queue (3 pieces)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianSurface)
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.next_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            uiState.nextQueue.forEachIndexed { idx, pair ->
                MiniPiecePreview(
                    type = pair.first,
                    hueOffset = pair.second,
                    colorPhaseProvider = colorPhaseProvider,
                    level = uiState.level,
                    mode = mode,
                    modifier = Modifier.size(if (idx == 0) 48.dp else 40.dp)
                )
                if (idx < uiState.nextQueue.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Level & Lines compact pills
        StatCard(
            label = stringResource(R.string.level_label),
            value = uiState.level.toString(),
            accent = PrismCyan
        )
        StatCard(
            label = stringResource(R.string.lines_label),
            value = uiState.lines.toString(),
            accent = PrismAmber
        )

        // Quick Color-Shift Mode switcher pill
        val modeLabel = when (mode) {
            ColorShiftMode.CONTINUOUS_SPECTRUM -> stringResource(R.string.mode_continuous)
            ColorShiftMode.LEVEL_GRADIENT -> stringResource(R.string.mode_level)
            ColorShiftMode.AURORA_PASTEL -> stringResource(R.string.mode_aurora)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianSurfaceElevated)
                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                .clickable(onClick = onCycleColorMode)
                .testTag("cycle_color_mode_button")
                .padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            LiveSpectrumBar(
                colorPhaseProvider = colorPhaseProvider,
                mode = mode,
                level = uiState.level,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = modeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ScoreHeaderRow(
    score: Int,
    highScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScoreBannerCard(
            label = stringResource(R.string.score_label),
            value = score,
            accent = PrismCyan,
            modifier = Modifier.weight(1f)
        )
        ScoreBannerCard(
            label = stringResource(R.string.best_label),
            value = highScore,
            accent = PrismAmber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreBannerCard(
    label: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = value.toString(),
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = accent
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    accent: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorderSubtle, RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp, horizontal = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
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
                val cellSize = (minOf(size.width / 4f, size.height / 3f)).coerceAtLeast(6f)
                val offsetX = (size.width - spanCols * cellSize) / 2f
                val offsetY = (size.height - spanRows * cellSize) / 2f
                val phase = colorPhaseProvider()

                for (pt in cells) {
                    val normCol = pt.col - minCol
                    val normRow = pt.row - minRow
                    val palette = SpectrumColorEngine.paletteForBlock(
                        baseHue = type.baseHue,
                        hueOffset = hueOffset,
                        colorPhase = phase,
                        row = normRow + 2,
                        col = normCol,
                        level = level,
                        mode = mode,
                        isLocked = false
                    )
                    drawMiniTile(
                        left = offsetX + normCol * cellSize,
                        top = offsetY + normRow * cellSize,
                        tileSize = cellSize,
                        body = palette.body,
                        highlight = palette.highlight
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniTile(
    left: Float,
    top: Float,
    tileSize: Float,
    body: Color,
    highlight: Color
) {
    val gap = (tileSize * 0.08f).coerceIn(1f, 2f)
    val s = (tileSize - gap * 2f).coerceAtLeast(2f)
    val radius = (s * 0.22f).coerceIn(2f, 5f)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(highlight, body),
            start = Offset(left + gap, top + gap),
            end = Offset(left + gap + s, top + gap + s)
        ),
        topLeft = Offset(left + gap, top + gap),
        size = androidx.compose.ui.geometry.Size(s, s),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
    )
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
        drawRect(
            brush = Brush.horizontalGradient(colors = listOf(c1, c2, c3))
        )
    }
}
