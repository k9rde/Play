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
import com.example.game.ColorShiftMode
import com.example.game.ColorShiftSpeed
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianBorderSubtle
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: StoredGameStatsAndSettings,
    onDismiss: () -> Unit,
    onSelectColorMode: (ColorShiftMode) -> Unit,
    onSelectColorSpeed: (ColorShiftSpeed) -> Unit,
    onToggleGhost: (Boolean) -> Unit,
    onToggleGestures: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
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
                color = TextPrimary
            )

            // Color Shift Mode Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.color_mode_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = PrismCyan
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
                    color = PrismMagenta
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
                    .background(ObsidianSurfaceElevated)
                    .border(1.dp, ObsidianBorderSubtle, RoundedCornerShape(16.dp))
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
private fun ColorModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTagName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ObsidianSurfaceElevated else ObsidianSurface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) PrismCyan else ObsidianBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTagName)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) PrismCyan else TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
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
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrismMagenta.copy(alpha = 0.22f),
            selectedLabelColor = PrismMagenta,
            containerColor = ObsidianSurfaceElevated,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = ObsidianBorder,
            selectedBorderColor = PrismMagenta
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
            color = TextPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = PrismCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianSurface
            )
        )
    }
}
