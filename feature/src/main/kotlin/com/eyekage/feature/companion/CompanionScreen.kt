package com.eyekage.feature.companion

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.behavior.PostureAlertState
import com.eyekage.core.model.HandGestureReaction
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
private const val COLOR_MORPH_DURATION_MS = 200
private const val DEFAULT_FRAME_DELTA_MS = 16L
private const val MAX_FRAME_DELTA_MS = 100L
private const val GAZE_TICKER_THRESHOLD_MS = 1000L

private val VoiceIndicatorActive = Color(0xFFF59E0B)

/**
 * Primary companion faceplate screen hosting procedural robot eyes, camera privacy indicators,
 * real-time gaze tracking smoothing, Awakening reaction overlays, and touch/poke feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionScreen(
    uiState: CompanionUiState,
    onEvent: (CompanionUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showModePicker by remember { mutableStateOf(false) }
    val targetSpec = remember(uiState.currentModeId) { EyeSpec.fromModeId(uiState.currentModeId) }
    val activeSpec = rememberAnimatedEyeSpec(targetSpec)

    val behaviorStateMachine = remember { CompanionBehaviorStateMachine() }
    val motionEngine = remember { EyeMotionEngine().apply { reducedMotion = uiState.reducedMotion } }
    var motionState by remember { mutableStateOf(EyeMotionState()) }
    var awakeningProgress by remember { mutableFloatStateOf(0.0f) }

    LaunchedEyeFrameLoop(
        uiState = uiState,
        behaviorStateMachine = behaviorStateMachine,
        motionEngine = motionEngine,
        onEvent = onEvent,
        onUpdateFrame = { newProgress, newState ->
            awakeningProgress = newProgress
            motionState = newState
        }
    )

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        RobotEyesCanvas(
            state = motionState,
            spec = activeSpec,
            microExpression = uiState.microExpression,
            eyeTiltDegrees = uiState.eyeTiltDegrees,
            leftEyeOpennessMultiplier = uiState.leftEyeOpennessMultiplier,
            rightEyeOpennessMultiplier = uiState.rightEyeOpennessMultiplier,
            onPoke = {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                behaviorStateMachine.poke(System.currentTimeMillis())
                onEvent(CompanionUiEvent.OnPoke())
            },
            modifier = Modifier.fillMaxSize()
        )
        AwakeningOverlay(progress = awakeningProgress, reducedMotion = uiState.reducedMotion)
        if (uiState.showDebugOverlay) {
            DebugFaceOverlay(observation = uiState.faceObservation)
        }
        CompanionTopControls(
            isCameraActive = uiState.isCameraTrackingActive,
            isVoiceActive = uiState.isVoiceListening,
            postureAlert = uiState.postureAlert,
            gestureLabel = uiState.activeGestureFeedbackLabel,
            onDismissGestureLabel = { onEvent(CompanionUiEvent.DismissGestureBadge) },
            onDismissPostureAlert = { onEvent(CompanionUiEvent.DismissPostureAlert) },
            onToggleDebug = { onEvent(CompanionUiEvent.ToggleDebugOverlay) },
            onNavigateToSettings = onNavigateToSettings
        )
        ModeDrawerMenuPill(onOpenPicker = { showModePicker = true })
        if (showModePicker) {
            ModePickerBottomSheet(
                currentModeId = uiState.currentModeId,
                onModeSelected = { onEvent(CompanionUiEvent.SelectMode(it)) },
                onDismissRequest = { showModePicker = false }
            )
        }
    }
}

@Composable
private fun LaunchedEyeFrameLoop(
    uiState: CompanionUiState,
    behaviorStateMachine: CompanionBehaviorStateMachine,
    motionEngine: EyeMotionEngine,
    onEvent: (CompanionUiEvent) -> Unit,
    onUpdateFrame: (Float, EyeMotionState) -> Unit
) {
    LaunchedEffect(Unit) {
        var lastTimeMs = 0L
        var accumulatedGazeMs = 0L
        var currentMotionState = EyeMotionState()
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val timeMs = frameTimeNanos / NANOS_TO_MILLIS
                val deltaMs = if (lastTimeMs == 0L) {
                    DEFAULT_FRAME_DELTA_MS
                } else {
                    (timeMs - lastTimeMs).coerceIn(0L, MAX_FRAME_DELTA_MS)
                }
                lastTimeMs = timeMs

                if (uiState.faceObservation.isFaceDetected) {
                    accumulatedGazeMs += deltaMs
                    if (accumulatedGazeMs >= GAZE_TICKER_THRESHOLD_MS) {
                        onEvent(CompanionUiEvent.RecordGazeDuration(accumulatedGazeMs / GAZE_TICKER_THRESHOLD_MS))
                        accumulatedGazeMs %= GAZE_TICKER_THRESHOLD_MS
                    }
                }

                val output = behaviorStateMachine.update(uiState.faceObservation, timeMs)
                val isFrozen = uiState.handGestureReaction is HandGestureReaction.HoldStop && uiState.handGestureReaction.isFrozen
                val isFist = uiState.handGestureReaction is HandGestureReaction.FistResolve
                val effectiveTarget = if (isFrozen) com.eyekage.core.motion.GazeTarget(currentMotionState.gazeX, currentMotionState.gazeY) else output.targetGaze
                val effectivePupilModifier = if (isFist) 0.75f else output.pupilScaleModifier
                val effectiveEyelidModifier = if (isFrozen) 1.0f else output.eyelidModifier

                val newState = motionEngine.update(
                    target = effectiveTarget,
                    timestampMs = timeMs,
                    eyelidModifier = effectiveEyelidModifier,
                    pupilScaleModifier = effectivePupilModifier
                )
                currentMotionState = newState
                onUpdateFrame(output.awakeningProgress, newState)
            }
        }
    }
}

@Composable
private fun rememberAnimatedEyeSpec(targetSpec: EyeSpec): EyeSpec {
    val primary by animateColorAsState(
        targetValue = targetSpec.primaryColor,
        animationSpec = tween(COLOR_MORPH_DURATION_MS, easing = FastOutSlowInEasing),
        label = "EyePrimaryMorph"
    )
    val secondary by animateColorAsState(
        targetValue = targetSpec.secondaryColor,
        animationSpec = tween(COLOR_MORPH_DURATION_MS, easing = FastOutSlowInEasing),
        label = "EyeSecondaryMorph"
    )
    val core by animateColorAsState(
        targetValue = targetSpec.coreColor,
        animationSpec = tween(COLOR_MORPH_DURATION_MS, easing = FastOutSlowInEasing),
        label = "EyeCoreMorph"
    )
    val glow by animateColorAsState(
        targetValue = targetSpec.glowColor,
        animationSpec = tween(COLOR_MORPH_DURATION_MS, easing = FastOutSlowInEasing),
        label = "EyeGlowMorph"
    )
    return remember(targetSpec.shape, primary, secondary, core, glow) {
        targetSpec.copy(primaryColor = primary, secondaryColor = secondary, coreColor = core, glowColor = glow)
    }
}

@Composable
private fun BoxScope.CompanionTopControls(
    isCameraActive: Boolean,
    isVoiceActive: Boolean,
    postureAlert: PostureAlertState,
    gestureLabel: String? = null,
    onDismissGestureLabel: () -> Unit = {},
    onDismissPostureAlert: () -> Unit,
    onToggleDebug: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (gestureLabel != null) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onDismissGestureLabel() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                androidx.compose.material3.Text(
                    text = gestureLabel,
                    color = Color(0xFF10B981),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                )
            }
        }
        if (postureAlert is PostureAlertState.AlertActive) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onDismissPostureAlert() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "🧘 Slouch",
                    color = Color(0xFFF59E0B),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                )
            }
        }
        if (isVoiceActive) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = VoiceIndicatorActive,
                        shape = CircleShape
                    )
            )
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isCameraActive) CameraIndicatorActive else CameraIndicatorInactive,
                    shape = CircleShape
                )
                .clickable { onToggleDebug() }
        )
    }

    IconButton(
        onClick = onNavigateToSettings,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun BoxScope.ModeDrawerMenuPill(
    onOpenPicker: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
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
            ) { onOpenPicker() }
    )
}
