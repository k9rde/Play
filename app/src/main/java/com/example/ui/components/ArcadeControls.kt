package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PrismAmber
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismMagenta
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArcadeControls(
    enabled: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onRotateCw: () -> Unit,
    onRotateCcw: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep physical Left/Right directions consistent even in RTL Arabic locales
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left cluster: directional pad (Left, Soft Drop, Right)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RepeatableArcadeButton(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescriptionText = stringResource(R.string.cd_move_left),
                    testTagName = "move_left_button",
                    enabled = enabled,
                    accent = TextPrimary,
                    onStep = onMoveLeft,
                    modifier = Modifier.size(56.dp)
                )
                RepeatableArcadeButton(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    contentDescriptionText = stringResource(R.string.cd_soft_drop),
                    testTagName = "soft_drop_button",
                    enabled = enabled,
                    accent = PrismCyan,
                    repeatInitialDelayMs = 120L,
                    repeatIntervalMs = 46L,
                    onStep = onSoftDrop,
                    modifier = Modifier.size(56.dp)
                )
                RepeatableArcadeButton(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescriptionText = stringResource(R.string.cd_move_right),
                    testTagName = "move_right_button",
                    enabled = enabled,
                    accent = TextPrimary,
                    onStep = onMoveRight,
                    modifier = Modifier.size(56.dp)
                )
            }

            // Right cluster: Hard Drop + Rotate CCW + Rotate CW
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TapArcadeButton(
                    icon = Icons.Rounded.KeyboardDoubleArrowDown,
                    contentDescriptionText = stringResource(R.string.cd_hard_drop),
                    testTagName = "hard_drop_button",
                    enabled = enabled,
                    accent = PrismAmber,
                    isPrimary = true,
                    onClick = onHardDrop,
                    modifier = Modifier.size(width = 56.dp, height = 56.dp)
                )
                TapArcadeButton(
                    icon = Icons.AutoMirrored.Rounded.RotateLeft,
                    contentDescriptionText = stringResource(R.string.cd_rotate_ccw),
                    testTagName = "rotate_ccw_button",
                    enabled = enabled,
                    accent = TextPrimary,
                    isPrimary = false,
                    onClick = onRotateCcw,
                    modifier = Modifier.size(52.dp)
                )
                TapArcadeButton(
                    icon = Icons.AutoMirrored.Rounded.RotateRight,
                    contentDescriptionText = stringResource(R.string.cd_rotate_cw),
                    testTagName = "rotate_cw_button",
                    enabled = enabled,
                    accent = PrismMagenta,
                    isPrimary = true,
                    onClick = onRotateCw,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}

@Composable
private fun RepeatableArcadeButton(
    icon: ImageVector,
    contentDescriptionText: String,
    testTagName: String,
    enabled: Boolean,
    accent: Color,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
    repeatInitialDelayMs: Long = 165L,
    repeatIntervalMs: Long = 52L
) {
    val currentOnStep by rememberUpdatedState(onStep)
    val currentEnabled by rememberUpdatedState(enabled)
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(16.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(ObsidianSurfaceElevated)
            .border(1.dp, ObsidianBorder, shape)
            .indication(interactionSource, ripple(color = accent))
            .semantics {
                role = Role.Button
                contentDescription = contentDescriptionText
            }
            .testTag(testTagName)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!currentEnabled) return@awaitEachGesture
                    val press = PressInteraction.Press(down.position)
                    scope.launch { interactionSource.emit(press) }

                    currentOnStep()
                    val repeatJob = scope.launch {
                        delay(repeatInitialDelayMs)
                        while (true) {
                            currentOnStep()
                            delay(repeatIntervalMs)
                        }
                    }

                    val up = waitForUpOrCancellation()
                    repeatJob.cancel()
                    scope.launch {
                        if (up != null) {
                            interactionSource.emit(PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                }
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescriptionText,
            tint = if (enabled) accent else accent.copy(alpha = 0.35f),
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun TapArcadeButton(
    icon: ImageVector,
    contentDescriptionText: String,
    testTagName: String,
    enabled: Boolean,
    accent: Color,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentEnabled by rememberUpdatedState(enabled)
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val shape = if (isPrimary) CircleShape else RoundedCornerShape(16.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(
                if (isPrimary) {
                    Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.24f),
                            ObsidianSurfaceElevated
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(ObsidianSurface, ObsidianSurfaceElevated))
                }
            )
            .border(
                width = 1.2.dp,
                color = if (isPrimary) accent.copy(alpha = 0.65f) else ObsidianBorder,
                shape = shape
            )
            .indication(interactionSource, ripple(color = accent))
            .semantics {
                role = Role.Button
                contentDescription = contentDescriptionText
            }
            .testTag(testTagName)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!currentEnabled) return@awaitEachGesture
                    val press = PressInteraction.Press(down.position)
                    scope.launch { interactionSource.emit(press) }
                    currentOnClick()
                    val up = waitForUpOrCancellation()
                    scope.launch {
                        if (up != null) {
                            interactionSource.emit(PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                }
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescriptionText,
            tint = if (enabled) accent else accent.copy(alpha = 0.35f),
            modifier = Modifier.size(28.dp)
        )
    }
}
