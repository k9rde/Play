package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StoredGameStatsAndSettings
import com.example.game.AppVisualTheme
import com.example.game.ColorShiftMode
import com.example.game.ColorShiftSpeed
import com.example.ui.theme.LocalGamePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: StoredGameStatsAndSettings,
    onDismiss: () -> Unit,
    onSelectVisualTheme: (AppVisualTheme) -> Unit,
    onSelectColorMode: (ColorShiftMode) -> Unit,
    onSelectColorSpeed: (ColorShiftSpeed) -> Unit,
    onToggleGhost: (Boolean) -> Unit,
    onToggleGestures: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit
) {
    val palette = LocalGamePalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary
            )

            // Visual Theme Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.visual_theme_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accentPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChip(
                        label = stringResource(R.string.theme_ceramic),
                        selected = settings.visualTheme == AppVisualTheme.DAYLIGHT_CERAMIC,
                        onClick = { onSelectVisualTheme(AppVisualTheme.DAYLIGHT_CERAMIC) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = stringResource(R.string.theme_midnight),
                        selected = settings.visualTheme == AppVisualTheme.MIDNIGHT_GLASS,
                        onClick = { onSelectVisualTheme(AppVisualTheme.MIDNIGHT_GLASS) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = stringResource(R.string.theme_retro),
                        selected = settings.visualTheme == AppVisualTheme.RETRO_SYNTH,
                        onClick = { onSelectVisualTheme(AppVisualTheme.RETRO_SYNTH) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Color Shift Mode Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.color_mode_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accentSecondary
                )
                ColorModeOption(
                    title = stringResource(R.string.mode_continuous),
                    description = stringResource(R.string.mode_continuous_desc),
                    selected = settings.colorShiftMode == ColorShiftMode.CONTINUOUS_SPECTRUM,
                    onClick = { onSelectColorMode(ColorShiftMode.CONTINUOUS_SPECTRUM) },
                    testTagName = "mode_continuous_option"
                )
                ColorModeOption(
                    title = stringResource(R.string.mode_level),
                    description = stringResource(R.string.mode_level_desc),
                    selected = settings.colorShiftMode == ColorShiftMode.LEVEL_GRADIENT,
                    onClick = { onSelectColorMode(ColorShiftMode.LEVEL_GRADIENT) },
                    testTagName = "mode_level_option"
                )
                ColorModeOption(
                    title = stringResource(R.string.mode_aurora),
                    description = stringResource(R.string.mode_aurora_desc),
                    selected = settings.colorShiftMode == ColorShiftMode.AURORA_PASTEL,
                    onClick = { onSelectColorMode(ColorShiftMode.AURORA_PASTEL) },
                    testTagName = "mode_aurora_option"
                )
            }

            // Color Shift Speed Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.color_speed_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accentTertiary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeedChip(
                        label = stringResource(R.string.speed_slow),
                        selected = settings.colorShiftSpeed == ColorShiftSpeed.SLOW,
                        onClick = { onSelectColorSpeed(ColorShiftSpeed.SLOW) },
                        modifier = Modifier.weight(1f)
                    )
                    SpeedChip(
                        label = stringResource(R.string.speed_medium),
                        selected = settings.colorShiftSpeed == ColorShiftSpeed.MEDIUM,
                        onClick = { onSelectColorSpeed(ColorShiftSpeed.MEDIUM) },
                        modifier = Modifier.weight(1f)
                    )
                    SpeedChip(
                        label = stringResource(R.string.speed_fast),
                        selected = settings.colorShiftSpeed == ColorShiftSpeed.FAST,
                        onClick = { onSelectColorSpeed(ColorShiftSpeed.FAST) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Gameplay Toggles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.bgTop.copy(alpha = 0.5f))
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                SettingToggleRow(
                    label = stringResource(R.string.ghost_toggle),
                    checked = settings.ghostEnabled,
                    onCheckedChange = onToggleGhost
                )
                SettingToggleRow(
                    label = stringResource(R.string.gestures_toggle),
                    checked = settings.gesturesEnabled,
                    onCheckedChange = onToggleGestures
                )
                SettingToggleRow(
                    label = stringResource(R.string.sound_toggle),
                    checked = settings.soundEnabled,
                    onCheckedChange = onToggleSound
                )
                SettingToggleRow(
                    label = stringResource(R.string.haptics_toggle),
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onToggleHaptics
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = palette.accentPrimary.copy(alpha = 0.18f),
            selectedLabelColor = palette.accentPrimary,
            containerColor = palette.bgTop.copy(alpha = 0.55f),
            labelColor = palette.textSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = palette.borderSubtle,
            selectedBorderColor = palette.accentPrimary
        ),
        modifier = modifier
    )
}

@Composable
private fun ColorModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTagName: String
) {
    val palette = LocalGamePalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) palette.accentSecondary.copy(alpha = 0.10f) else palette.bgTop.copy(alpha = 0.45f))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) palette.accentSecondary else palette.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTagName)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) palette.accentSecondary else palette.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun SpeedChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = palette.accentTertiary.copy(alpha = 0.20f),
            selectedLabelColor = palette.accentTertiary,
            containerColor = palette.bgTop.copy(alpha = 0.55f),
            labelColor = palette.textSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = palette.borderSubtle,
            selectedBorderColor = palette.accentTertiary
        ),
        modifier = modifier
    )
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalGamePalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.surface,
                checkedTrackColor = palette.accentPrimary,
                uncheckedThumbColor = palette.textSecondary,
                uncheckedTrackColor = palette.borderSubtle
            )
        )
    }
}
