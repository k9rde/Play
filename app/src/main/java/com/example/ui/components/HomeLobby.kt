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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.AppVisualTheme
import com.example.game.ColorShiftMode
import com.example.game.GameUiState
import com.example.game.SpectrumColorEngine
import com.example.game.TetrominoType
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGamePalette
import com.example.ui.theme.paletteForTheme
import kotlin.math.sin

@Composable
fun HomeLobby(
    uiState: GameUiState,
    colorPhaseProvider: () -> Float,
    onStartGame: () -> Unit,
    onSelectVisualTheme: (AppVisualTheme) -> Unit,
    onSelectColorMode: (ColorShiftMode) -> Unit,
    onStartingLevelChange: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    val settings = uiState.statsAndSettings

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.bgTop, palette.bgBottom)
                )
            )
    ) {
        // Subtle animated floating decorative spectrum blocks in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val phase = colorPhaseProvider()
            val mode = settings.colorShiftMode
            val decoPieces = listOf(
                Triple(TetrominoType.T, 0.14f, 0.14f),
                Triple(TetrominoType.L, 0.84f, 0.20f),
                Triple(TetrominoType.S, 0.10f, 0.72f),
                Triple(TetrominoType.J, 0.86f, 0.78f)
            )
            decoPieces.forEachIndexed { idx, (type, xRatio, yRatio) ->
                val bob = sin(Math.toRadians((phase * 1.3 + idx * 75).toDouble())).toFloat() * 14.dp.toPx()
                val cellSize = 18.dp.toPx()
                val originX = size.width * xRatio - cellSize * 1.5f
                val originY = size.height * yRatio + bob
                for (pt in type.cells(idx % 4)) {
                    val pal = SpectrumColorEngine.paletteForBlock(
                        baseHue = type.baseHue,
                        hueOffset = idx * 45f,
                        colorPhase = phase,
                        row = pt.row + idx * 3,
                        col = pt.col,
                        level = uiState.startingLevel,
                        mode = mode,
                        isLocked = false
                    )
                    drawRoundRect(
                        color = pal.body.copy(alpha = if (palette.isLight) 0.16f else 0.14f),
                        topLeft = Offset(originX + pt.col * cellSize, originY + pt.row * cellSize),
                        size = Size(cellSize * 0.88f, cellSize * 0.88f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .widthIn(max = 460.dp)
                .align(Alignment.Center)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Hero Emblem + Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(10.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.boardBgTop)
                        .border(1.5.dp, palette.accentPrimary.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                ) {
                    MiniPiecePreview(
                        type = TetrominoType.T,
                        hueOffset = 20f,
                        colorPhaseProvider = colorPhaseProvider,
                        level = uiState.startingLevel,
                        mode = settings.colorShiftMode,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.subtitle_spectrum),
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Personal Best Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (palette.isLight) 4.dp else 0.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LobbyStatItem(
                    label = stringResource(R.string.best_label),
                    value = uiState.highScore.toString(),
                    accent = palette.accentTertiary,
                    showTrophy = uiState.highScore > 0
                )
                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 32.dp)
                        .background(palette.borderSubtle)
                )
                LobbyStatItem(
                    label = stringResource(R.string.total_lines_stat),
                    value = settings.totalLinesCleared.toString(),
                    accent = palette.accentPrimary,
                    showTrophy = false
                )
                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 32.dp)
                        .background(palette.borderSubtle)
                )
                LobbyStatItem(
                    label = stringResource(R.string.best_level_stat),
                    value = settings.bestLevel.toString(),
                    accent = palette.accentSecondary,
                    showTrophy = false
                )
            }

            // Visual Theme Picker Card (3 clean swatches)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (palette.isLight) 4.dp else 0.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.visual_theme_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionCard(
                        theme = AppVisualTheme.DAYLIGHT_CERAMIC,
                        label = stringResource(R.string.theme_ceramic),
                        selected = settings.visualTheme == AppVisualTheme.DAYLIGHT_CERAMIC,
                        onClick = { onSelectVisualTheme(AppVisualTheme.DAYLIGHT_CERAMIC) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        theme = AppVisualTheme.MIDNIGHT_GLASS,
                        label = stringResource(R.string.theme_midnight),
                        selected = settings.visualTheme == AppVisualTheme.MIDNIGHT_GLASS,
                        onClick = { onSelectVisualTheme(AppVisualTheme.MIDNIGHT_GLASS) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        theme = AppVisualTheme.RETRO_SYNTH,
                        label = stringResource(R.string.theme_retro),
                        selected = settings.visualTheme == AppVisualTheme.RETRO_SYNTH,
                        onClick = { onSelectVisualTheme(AppVisualTheme.RETRO_SYNTH) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Color Shift Mode Quick Selector + Starting Level
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (palette.isLight) 4.dp else 0.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.color_mode_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textPrimary
                    )
                    LiveSpectrumBar(
                        colorPhaseProvider = colorPhaseProvider,
                        mode = settings.colorShiftMode,
                        level = uiState.startingLevel,
                        modifier = Modifier
                            .size(width = 76.dp, height = 7.dp)
                            .clip(CircleShape)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorModePill(
                        label = stringResource(R.string.mode_continuous),
                        selected = settings.colorShiftMode == ColorShiftMode.CONTINUOUS_SPECTRUM,
                        onClick = { onSelectColorMode(ColorShiftMode.CONTINUOUS_SPECTRUM) },
                        modifier = Modifier.weight(1f)
                    )
                    ColorModePill(
                        label = stringResource(R.string.mode_level),
                        selected = settings.colorShiftMode == ColorShiftMode.LEVEL_GRADIENT,
                        onClick = { onSelectColorMode(ColorShiftMode.LEVEL_GRADIENT) },
                        modifier = Modifier.weight(1f)
                    )
                    ColorModePill(
                        label = stringResource(R.string.mode_aurora),
                        selected = settings.colorShiftMode == ColorShiftMode.AURORA_PASTEL,
                        onClick = { onSelectColorMode(ColorShiftMode.AURORA_PASTEL) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.bgTop.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.starting_level_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { onStartingLevelChange(uiState.startingLevel - 1) },
                            enabled = uiState.startingLevel > 1,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = palette.surface,
                                contentColor = palette.textPrimary
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "-")
                        }
                        Text(
                            text = uiState.startingLevel.toString(),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = palette.accentPrimary
                        )
                        FilledTonalIconButton(
                            onClick = { onStartingLevelChange(uiState.startingLevel + 1) },
                            enabled = uiState.startingLevel < 10,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = palette.surface,
                                contentColor = palette.textPrimary
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "+")
                        }
                    }
                }
            }

            // Primary Play CTA + Settings button
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accentPrimary,
                    contentColor = if (palette.isLight) Color.White else palette.bgBottom
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .testTag("start_game_button")
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.start_game),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("overlay_settings_button")
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = null, tint = palette.accentSecondary)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textPrimary
                )
            }

            Text(
                text = stringResource(R.string.how_to_play_hint),
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun LobbyStatItem(
    label: String,
    value: String,
    accent: Color,
    showTrophy: Boolean
) {
    val palette = LocalGamePalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showTrophy) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = accent
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun ThemeOptionCard(
    theme: AppVisualTheme,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePalette = LocalGamePalette.current
    val preview = paletteForTheme(theme)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) activePalette.accentPrimary.copy(alpha = 0.12f) else activePalette.bgTop.copy(alpha = 0.45f))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) activePalette.accentPrimary else activePalette.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(preview.bgTop)
                    .border(1.dp, preview.border, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(preview.accentPrimary)
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(preview.accentSecondary)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) activePalette.accentPrimary else activePalette.textPrimary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun ColorModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) palette.accentSecondary.copy(alpha = 0.15f) else palette.bgTop.copy(alpha = 0.45f))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) palette.accentSecondary else palette.borderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) palette.accentSecondary else palette.textPrimary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}
