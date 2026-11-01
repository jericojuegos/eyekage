package com.eyekage.feature.companion

import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.motion.EyeMotionEngine
import com.eyekage.core.motion.EyeMotionState
import com.eyekage.ui.eyes.AwakeningOverlay
import com.eyekage.ui.eyes.EyeSpec
import com.eyekage.ui.eyes.RobotEyesCanvas
import com.eyekage.ui.theme.CameraIndicatorActive
import com.eyekage.ui.theme.CameraIndicatorInactive
import com.eyekage.ui.theme.DarkBackground
import com.eyekage.ui.theme.DarkOutline
import kotlinx.coroutines.isActive

private const val NANOS_TO_MILLIS = 1_000_000L

/**
 * Primary companion faceplate screen hosting procedural robot eyes, camera privacy indicators,
 * real-time gaze tracking smoothing, Awakening reaction overlays, and touch/poke feedback.
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
    val view = LocalView.current
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

    // High-level behavior state machine driving reactions, awakening, searching, and pokes
    val behaviorStateMachine = remember { CompanionBehaviorStateMachine() }

    // Drive procedural eye motion engine at display frame rate (60/90 Hz)
    val motionEngine = remember { EyeMotionEngine() }
    motionEngine.reducedMotion = uiState.reducedMotion

    var motionState by remember { mutableStateOf(EyeMotionState()) }
    var awakeningProgress by remember { mutableFloatStateOf(0.0f) }

    // Frame-rate synchronized render loop updating behavior state and One-Euro smoothed gaze
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val timeMs = frameTimeNanos / NANOS_TO_MILLIS
                val output = behaviorStateMachine.update(
                    observation = uiState.faceObservation,
                    timestampMs = timeMs
                )
                awakeningProgress = output.awakeningProgress
                motionState = motionEngine.update(
                    target = output.targetGaze,
                    timestampMs = timeMs,
                    eyelidModifier = output.eyelidModifier,
                    pupilScaleModifier = output.pupilScaleModifier
                )
            }
        }
    }

    val onPokeTriggered: () -> Unit = {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        behaviorStateMachine.poke(System.currentTimeMillis())
        onEvent(CompanionUiEvent.OnPoke())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Zero-allocation procedural robot eye canvas with touch/poke detector
        RobotEyesCanvas(
            state = motionState,
            spec = activeSpec,
            onPoke = { onPokeTriggered() },
            modifier = Modifier.fillMaxSize()
        )

        // Luminous radial amber burst overlay during Awakening reaction
        AwakeningOverlay(
            progress = awakeningProgress,
            reducedMotion = uiState.reducedMotion
        )

        // Debug bounding box & coordinate shape overlay (debug only, zero raw pixels)
        if (uiState.showDebugOverlay) {
            DebugFaceOverlay(observation = uiState.faceObservation)
        }

        // Privacy Camera Active/Inactive Indicator (top-end, tap to toggle debug shapes)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(12.dp)
                .background(
                    color = if (uiState.isCameraTrackingActive) CameraIndicatorActive else CameraIndicatorInactive,
                    shape = CircleShape
                )
                .clickable {
                    onEvent(CompanionUiEvent.ToggleDebugOverlay)
                }
        )

        // Subtle mode switcher drawer menu pill (bottom-center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .width(48.dp)
                .height(5.dp)
                .background(DarkOutline, RoundedCornerShape(2.5.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    showModePicker = true
                }
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
