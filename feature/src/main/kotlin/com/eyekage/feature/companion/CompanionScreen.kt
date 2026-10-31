package com.eyekage.feature.companion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eyekage.core.motion.EyeMotionEngine
import com.eyekage.core.motion.EyeMotionState
import com.eyekage.core.motion.GazeTarget
import com.eyekage.ui.eyes.EyeSpec
import com.eyekage.ui.eyes.RobotEyesCanvas
import com.eyekage.ui.theme.CameraIndicatorActive
import com.eyekage.ui.theme.CameraIndicatorInactive
import com.eyekage.ui.theme.DarkBackground
import com.eyekage.ui.theme.DarkOutline
import kotlinx.coroutines.isActive

/**
 * Primary companion faceplate screen hosting procedural robot eyes, camera privacy indicators,
 * and the data-driven eye mode switcher bottom sheet.
 *
 * @param uiState Immutable state for companion mode, tracking state, and active eye mode.
 * @param onEvent Discrete UI event callback.
 * @param modifier Root layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionScreen(
    uiState: CompanionUiState,
    onEvent: (CompanionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showModePicker by remember { mutableStateOf(false) }

    // Resolve target eye styling from active mode identifier
    val targetSpec = remember(uiState.currentModeId) {
        EyeSpec.fromModeId(uiState.currentModeId)
    }

    // 200 ms smooth color morph between mode transitions
    val animatedPrimary by animateColorAsState(
        targetValue = targetSpec.primaryColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "EyePrimaryColorMorph"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetSpec.secondaryColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "EyeSecondaryColorMorph"
    )
    val animatedCore by animateColorAsState(
        targetValue = targetSpec.coreColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "EyeCoreColorMorph"
    )
    val animatedGlow by animateColorAsState(
        targetValue = targetSpec.glowColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "EyeGlowColorMorph"
    )

    val activeSpec = remember(targetSpec.shape, animatedPrimary, animatedSecondary, animatedCore, animatedGlow) {
        targetSpec.copy(
            primaryColor = animatedPrimary,
            secondaryColor = animatedSecondary,
            coreColor = animatedCore,
            glowColor = animatedGlow
        )
    }

    // Drive procedural eye motion engine at display frame rate (60/90 Hz)
    val motionEngine = remember { EyeMotionEngine() }
    var motionState by remember { mutableStateOf(EyeMotionState()) }

    LaunchedEffect(uiState.faceObservation) {
        val targetGaze = if (uiState.faceObservation.isFaceDetected) {
            GazeTarget(
                x = uiState.faceObservation.normalizedX.coerceIn(-1.0f, 1.0f),
                y = uiState.faceObservation.normalizedY.coerceIn(-1.0f, 1.0f)
            )
        } else {
            GazeTarget.Center
        }

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val timeMs = frameTimeNanos / 1_000_000L
                motionState = motionEngine.update(target = targetGaze, timestampMs = timeMs)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onEvent(CompanionUiEvent.OnScreenTapped)
                showModePicker = true
            }
    ) {
        // Zero-allocation procedural robot eye canvas
        RobotEyesCanvas(
            state = motionState,
            spec = activeSpec,
            modifier = Modifier.fillMaxSize()
        )

        // Privacy Camera Active/Inactive Indicator (top-end)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(10.dp)
                .background(
                    color = if (uiState.isCameraTrackingActive) CameraIndicatorActive else CameraIndicatorInactive,
                    shape = CircleShape
                )
        )

        // Subtle mode switcher drawer menu pill (bottom-center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .width(48.dp)
                .height(5.dp)
                .background(DarkOutline, RoundedCornerShape(2.5.dp))
                .clickable { showModePicker = true }
        )

        // Mode Switcher Bottom Sheet
        if (showModePicker) {
            ModePickerBottomSheet(
                currentModeId = uiState.currentModeId,
                onModeSelected = { selectedModeId ->
                    onEvent(CompanionUiEvent.SelectMode(selectedModeId))
                },
                onDismissRequest = {
                    showModePicker = false
                }
            )
        }
    }
}
