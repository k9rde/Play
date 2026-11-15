package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameStatus
import com.example.game.GameUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismAmber
import com.example.ui.theme.PrismCoral
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameStatusOverlay(
    uiState: GameUiState,
    colorPhaseProvider: () -> Float,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartingLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.status == GameStatus.PLAYING) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xB8060812))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ObsidianSurfaceElevated, ObsidianSurface)
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(PrismCyan.copy(alpha = 0.65f), ObsidianBorder, PrismMagenta.copy(alpha = 0.65f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(22.dp)
        ) {
            LiveSpectrumBar(
                colorPhaseProvider = colorPhaseProvider,
                mode = uiState.statsAndSettings.colorShiftMode,
                level = uiState.level,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            when (uiState.status) {
                GameStatus.READY -> {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.subtitle_spectrum),
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrismCyan,
                        textAlign = TextAlign.Center
                    )

                    // Starting Level picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianBg.copy(alpha = 0.6f))
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.starting_level_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { onStartingLevelChange(uiState.startingLevel - 1) },
                                enabled = uiState.startingLevel > 1,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = ObsidianSurfaceElevated,
                                    contentColor = TextPrimary
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
                                color = PrismAmber
                            )
                            FilledTonalIconButton(
                                onClick = { onStartingLevelChange(uiState.startingLevel + 1) },
                                enabled = uiState.startingLevel < 10,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = ObsidianSurfaceElevated,
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "+")
                            }
                        }
                    }

                    if (uiState.highScore > 0) {
                        Text(
                            text = "${stringResource(R.string.best_label)}: ${uiState.highScore}  •  ${stringResource(R.string.total_lines_stat, uiState.statsAndSettings.totalLinesCleared)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = onStartGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrismCyan,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_game_button")
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
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
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = PrismMagenta)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.settings_title),
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = stringResource(R.string.how_to_play_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                GameStatus.PAUSED -> {
                    Text(
                        text = stringResource(R.string.paused_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Button(
                        onClick = onResumeGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrismCyan,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("resume_game_button")
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.resume_game),
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
                    ) {
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = PrismMagenta)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.settings_title), color = TextPrimary)
                    }
                    OutlinedButton(
                        onClick = onStartGame,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restart_from_pause_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = PrismAmber)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.new_game), color = TextPrimary)
                    }
                }

                GameStatus.GAME_OVER -> {
                    Text(
                        text = stringResource(R.string.game_over_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PrismCoral
                    )
                    if (uiState.isNewRecord) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PrismAmber.copy(alpha = 0.18f))
                                .border(1.dp, PrismAmber, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = PrismAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.new_record_badge),
                                style = MaterialTheme.typography.labelLarge,
                                color = PrismAmber
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryStatBox(
                            label = stringResource(R.string.score_label),
                            value = uiState.score.toString(),
                            accent = PrismCyan,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatBox(
                            label = stringResource(R.string.level_label),
                            value = uiState.level.toString(),
                            accent = PrismMagenta,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatBox(
                            label = stringResource(R.string.lines_label),
                            value = uiState.lines.toString(),
                            accent = PrismAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = onStartGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrismCyan,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("play_again_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.restart_game),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                GameStatus.PLAYING -> Unit
            }
        }
    }
}

@Composable
private fun SummaryStatBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBg.copy(alpha = 0.65f))
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = accent
        )
    }
}
