package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
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
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.game.HapticCue
import com.example.ui.components.ArcadeControls
import com.example.ui.components.BoardCanvas
import com.example.ui.components.CompactGameHudStrip
import com.example.ui.components.GameStatusOverlay
import com.example.ui.components.HomeLobby
import com.example.ui.components.LiveSpectrumBar
import com.example.ui.components.SettingsSheet
import com.example.ui.theme.LocalGamePalette
import com.example.ui.theme.SpectrumBlocksTheme

@Composable
fun SpectrumBlocksApp(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpectrumBlocksTheme(visualTheme = uiState.statsAndSettings.visualTheme) {
        SpectrumBlocksScreen(
            uiState = uiState,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@Composable
private fun SpectrumBlocksScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    val hapticFeedback = LocalHapticFeedback.current
    var colorPhase by remember { mutableFloatStateOf(0f) }

    // Pause active game on system Back press instead of immediately exiting
    BackHandler(enabled = uiState.status == GameStatus.PLAYING) {
        viewModel.pauseGame()
    }

    LaunchedEffect(uiState.status, uiState.statsAndSettings.colorShiftSpeed) {
        val baseRate = uiState.statsAndSettings.colorShiftSpeed.degreesPerSecond
        val speedDegPerSec = when (uiState.status) {
            GameStatus.PLAYING -> baseRate
            GameStatus.READY -> baseRate * 0.70f
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
                    colors = listOf(palette.bgTop, palette.bgBottom)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (uiState.status == GameStatus.READY) {
            HomeLobby(
                uiState = uiState,
                colorPhaseProvider = { colorPhase },
                onStartGame = viewModel::startNewGame,
                onSelectVisualTheme = viewModel::updateVisualTheme,
                onSelectColorMode = viewModel::updateColorShiftMode,
                onStartingLevelChange = viewModel::setStartingLevel,
                onOpenSettings = viewModel::openSettings
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 540.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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

                CompactGameHudStrip(
                    uiState = uiState,
                    colorPhaseProvider = { colorPhase },
                    onHoldClick = viewModel::holdCurrentPiece,
                    onCycleColorMode = cycleColorMode
                )

                // Hero centered 10x20 Playfield with generous proportions
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
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

                ArcadeControls(
                    enabled = uiState.status == GameStatus.PLAYING,
                    onMoveLeft = viewModel::moveLeft,
                    onMoveRight = viewModel::moveRight,
                    onSoftDrop = { viewModel.softDrop() },
                    onHardDrop = viewModel::hardDrop,
                    onRotateCw = viewModel::rotateClockwise,
                    onRotateCcw = viewModel::rotateCounterClockwise,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            GameStatusOverlay(
                uiState = uiState,
                colorPhaseProvider = { colorPhase },
                onStartGame = viewModel::startNewGame,
                onResumeGame = viewModel::resumeGame,
                onOpenSettings = viewModel::openSettings,
                onReturnHome = viewModel::returnToHome
            )
        }

        if (uiState.showSettings) {
            SettingsSheet(
                settings = uiState.statsAndSettings,
                onDismiss = viewModel::closeSettings,
                onSelectVisualTheme = viewModel::updateVisualTheme,
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
    val palette = LocalGamePalette.current

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
                    .size(20.dp)
                    .clip(CircleShape)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleSound,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = palette.surface,
                    contentColor = if (soundEnabled) palette.accentPrimary else palette.textSecondary
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
                    containerColor = palette.surface,
                    contentColor = palette.accentSecondary
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

            IconButton(
                onClick = onTogglePause,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = palette.surface,
                    contentColor = palette.textPrimary
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
