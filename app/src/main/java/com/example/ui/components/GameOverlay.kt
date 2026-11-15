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
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameStatus
import com.example.game.GameUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGamePalette

@Composable
fun GameStatusOverlay(
    uiState: GameUiState,
    colorPhaseProvider: () -> Float,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onReturnHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.status != GameStatus.PAUSED && uiState.status != GameStatus.GAME_OVER) return

    val palette = LocalGamePalette.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 350.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(palette.surface)
                .border(1.5.dp, palette.border, RoundedCornerShape(26.dp))
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
                GameStatus.PAUSED -> {
                    Text(
                        text = stringResource(R.string.paused_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                    Button(
                        onClick = onResumeGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accentPrimary,
                            contentColor = if (palette.isLight) Color.White else palette.bgBottom
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
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = palette.accentSecondary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.settings_title), color = palette.textPrimary)
                    }
                    OutlinedButton(
                        onClick = onStartGame,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restart_from_pause_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = palette.accentTertiary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.new_game), color = palette.textPrimary)
                    }
                    OutlinedButton(
                        onClick = onReturnHome,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("return_home_button")
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null, tint = palette.textSecondary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.back_to_home), color = palette.textPrimary)
                    }
                }

                GameStatus.GAME_OVER -> {
                    Text(
                        text = stringResource(R.string.game_over_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.danger
                    )
                    if (uiState.isNewRecord) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(palette.accentTertiary.copy(alpha = 0.16f))
                                .border(1.dp, palette.accentTertiary, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = palette.accentTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.new_record_badge),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.accentTertiary
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
                            accent = palette.accentPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatBox(
                            label = stringResource(R.string.level_label),
                            value = uiState.level.toString(),
                            accent = palette.accentSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatBox(
                            label = stringResource(R.string.lines_label),
                            value = uiState.lines.toString(),
                            accent = palette.accentTertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = onStartGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accentPrimary,
                            contentColor = if (palette.isLight) Color.White else palette.bgBottom
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

                    OutlinedButton(
                        onClick = onReturnHome,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("game_over_home_button")
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null, tint = palette.textSecondary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.back_to_home), color = palette.textPrimary)
                    }
                }

                else -> Unit
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
    val palette = LocalGamePalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.bgTop.copy(alpha = 0.65f))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary
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
