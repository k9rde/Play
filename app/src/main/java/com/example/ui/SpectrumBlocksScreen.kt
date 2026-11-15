package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.game.ColorShiftMode
import com.example.game.GameStatus
import com.example.game.GameViewModel
import com.example.game.HapticCue
import com.example.ui.components.ArcadeControls
import com.example.ui.components.BoardCanvas
import com.example.ui.components.GameStatusOverlay
import com.example.ui.components.LiveSpectrumBar
import com.example.ui.components.ScoreHeaderRow
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SideHudColumn
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgSecondary
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismMagenta
import com.example.ui.theme.TextPrimary

@Composable
fun SpectrumBlocksScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current
    var colorPhase by remember { mutableFloatStateOf(0f) }

    // Drive smooth 60fps gradual color shifting without triggering Compose tree recompositions
    LaunchedEffect(uiState.status, uiState.statsAndSettings.colorShiftSpeed) {
        val baseRate = uiState.statsAndSettings.colorShiftSpeed.degreesPerSecond
        val speedDegPerSec = when (uiState.status) {
            GameStatus.PLAYING -> baseRate
            GameStatus.READY -> baseRate * 0.65f
            GameStatus.PAUSED, GameStatus.GAME_OVER -> 0f
        }
        if (speedDegPerSec > 0f) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dtSec = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        colorPhase = (colorPhase + speedDegPerSec * dtSec) % 360f
                    }
                    lastNanos = frameNanos
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.hapticCues.collect { cue ->
            when (cue) {
                HapticCue.TAP -> hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticCue.HARD_DROP,
                HapticCue.LINE_CLEAR,
                HapticCue.GAME_OVER -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val cycleColorMode = {
        val entries = ColorShiftMode.entries
        val nextIdx = (entries.indexOf(uiState.statsAndSettings.colorShiftMode) + 1) % entries.size
        viewModel.updateColorShiftMode(entries[nextIdx])
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianBgSecondary, ObsidianBg, ObsidianBg)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val isWideLayout = maxWidth >= 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopArcadeHeader(
                    status = uiState.status,
                    soundEnabled = uiState.statsAndSettings.soundEnabled,
                    colorPhaseProvider = { colorPhase },
                    mode = uiState.statsAndSettings.colorShiftMode,
                    level = uiState.level,
                    onToggleSound = viewModel::toggleSound,
                    onOpenSettings = viewModel::openSettings,
                    onTogglePause = viewModel::togglePause
                )

                ScoreHeaderRow(
                    score = uiState.score,
                    highScore = uiState.highScore
                )

                // Center Playfield + Side HUD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            BoardCanvas(
                                uiState = uiState,
                                colorPhaseProvider = { colorPhase },
                                gesturesEnabled = uiState.statsAndSettings.gesturesEnabled,
                                ghostEnabled = uiState.statsAndSettings.ghostEnabled,
                                onTapRotate = viewModel::rotateClockwise,
                                onMoveLeft = viewModel::moveLeft,
                                onMoveRight = viewModel::moveRight,
                                onSoftDrop = viewModel::softDrop,
                                onHardDrop = viewModel::hardDrop,
                                modifier = Modifier.fillMaxHeight()
                            )
                        }

                        SideHudColumn(
                            uiState = uiState,
                            colorPhaseProvider = { colorPhase },
                            onHoldClick = viewModel::holdCurrentPiece,
                            onCycleColorMode = cycleColorMode,
                            modifier = Modifier.width(if (isWideLayout) 136.dp else 100.dp)
                        )
                    }
                }

                // Bottom Tactile Controls
                ArcadeControls(
                    enabled = uiState.status == GameStatus.PLAYING,
                    onMoveLeft = viewModel::moveLeft,
                    onMoveRight = viewModel::moveRight,
                    onSoftDrop = { viewModel.softDrop() },
                    onHardDrop = viewModel::hardDrop,
                    onRotateCw = viewModel::rotateClockwise,
                    onRotateCcw = viewModel::rotateCounterClockwise,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            }
        }

        // Ready / Paused / Game Over Modal Overlay
        GameStatusOverlay(
            uiState = uiState,
            colorPhaseProvider = { colorPhase },
            onStartGame = viewModel::startNewGame,
            onResumeGame = viewModel::resumeGame,
            onOpenSettings = viewModel::openSettings,
            onStartingLevelChange = viewModel::setStartingLevel
        )

        // Color & Control Settings Sheet
        if (uiState.showSettings) {
            SettingsSheet(
                settings = uiState.statsAndSettings,
                onDismiss = viewModel::closeSettings,
                onSelectColorMode = viewModel::updateColorShiftMode,
                onSelectColorSpeed = viewModel::updateColorShiftSpeed,
                onToggleGhost = viewModel::setGhostEnabled,
                onToggleGestures = viewModel::setGesturesEnabled,
                onToggleSound = viewModel::setSoundEnabled,
                onToggleHaptics = viewModel::setHapticsEnabled
            )
        }
    }
}

@Composable
private fun TopArcadeHeader(
    status: GameStatus,
    soundEnabled: Boolean,
    colorPhaseProvider: () -> Float,
    mode: ColorShiftMode,
    level: Int,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
    onTogglePause: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveSpectrumBar(
                colorPhaseProvider = colorPhaseProvider,
                mode = mode,
                level = level,
                modifier = Modifier
                    .size(width = 22.dp, height = 22.dp)
                    .clip(CircleShape)
            )
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleSound,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ObsidianSurfaceElevated,
                    contentColor = if (soundEnabled) PrismCyan else TextPrimary.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (soundEnabled) {
                        Icons.AutoMirrored.Rounded.VolumeUp
                    } else {
                        Icons.AutoMirrored.Rounded.VolumeOff
                    },
                    contentDescription = stringResource(
                        if (soundEnabled) R.string.cd_sound_on else R.string.cd_sound_off
                    )
                )
            }

            IconButton(
                onClick = onOpenSettings,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ObsidianSurfaceElevated,
                    contentColor = PrismMagenta
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.cd_settings)
                )
            }

            if (status == GameStatus.PLAYING || status == GameStatus.PAUSED) {
                IconButton(
                    onClick = onTogglePause,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ObsidianSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pause_toggle_button")
                ) {
                    Icon(
                        imageVector = if (status == GameStatus.PLAYING) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (status == GameStatus.PLAYING) R.string.cd_pause else R.string.cd_resume
                        )
                    )
                }
            }
        }
    }
}
