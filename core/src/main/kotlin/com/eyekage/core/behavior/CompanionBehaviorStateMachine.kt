package com.eyekage.core.behavior

import com.eyekage.core.model.FaceObservation
import com.eyekage.core.motion.GazeTarget
import kotlin.math.PI
import kotlin.math.sin

private const val DEFAULT_EYELID = 1.0f
private const val DEFAULT_PUPIL_SCALE = 1.0f
private const val AWAKENING_EYELID = 1.05f
private const val AWAKENING_PUPIL_EXPANSION = 0.25f
private const val SEARCHING_EYELID = 0.90f
private const val SEARCHING_SWEEP_AMPLITUDE = 0.45f
private const val SEARCHING_SWEEP_FREQ = 0.8
private const val POKED_MIN_EYELID = 0.15f
private const val POKED_EYELID_RANGE = 0.85f
private const val POKED_PUPIL_SCALE = 0.85f
private const val MID_PROGRESS = 0.5f
private const val FULL_PROGRESS = 1.0f
private const val ZERO_PROGRESS = 0.0f
private const val SECONDS_DIVISOR = 1000.0
private const val TWO_PI = 2.0 * PI

/**
 * Pure-Kotlin behavioral state machine governing the companion robot's high-level reactions.
 */
class CompanionBehaviorStateMachine(
    initialState: CompanionState = CompanionState.Idle
) {

    private var state: CompanionState = initialState
    private var stateEnterTimeMs: Long = 0L
    private var lastObservation: FaceObservation = FaceObservation.Empty
    private var lastTargetGaze: GazeTarget = GazeTarget.Center

    val currentState: CompanionState
        get() = state

    fun reset(currentTimeMs: Long = 0L) {
        state = CompanionState.Idle
        stateEnterTimeMs = currentTimeMs
        lastObservation = FaceObservation.Empty
        lastTargetGaze = GazeTarget.Center
    }

    fun poke(timestampMs: Long) {
        val prevState = when (val current = state) {
            is CompanionState.Poked -> current.previousState
            else -> current
        }
        state = CompanionState.Poked(
            elapsedMs = 0L,
            totalDurationMs = CompanionState.POKED_DURATION_MS,
            previousState = prevState
        )
        stateEnterTimeMs = timestampMs
    }

    fun update(
        observation: FaceObservation,
        timestampMs: Long
    ): BehaviorOutput {
        lastObservation = observation
        if (stateEnterTimeMs == 0L) {
            stateEnterTimeMs = timestampMs
        }

        val hasFace = observation.isFaceDetected
        val faceGaze = resolveFaceGaze(observation, hasFace)
        val elapsed = (timestampMs - stateEnterTimeMs).coerceAtLeast(0L)

        dispatchStateTransition(hasFace, elapsed, timestampMs)
        return computeOutput(faceGaze, hasFace)
    }

    private fun resolveFaceGaze(observation: FaceObservation, hasFace: Boolean): GazeTarget {
        return if (hasFace) {
            val target = GazeTarget(
                x = observation.normalizedX.coerceIn(-1.0f, 1.0f),
                y = observation.normalizedY.coerceIn(-1.0f, 1.0f)
            )
            lastTargetGaze = target
            target
        } else {
            lastTargetGaze
        }
    }

    private fun dispatchStateTransition(hasFace: Boolean, elapsed: Long, timestampMs: Long) {
        when (val current = state) {
            is CompanionState.Idle -> handleIdleTransition(hasFace, timestampMs)
            is CompanionState.Awakening -> handleAwakeningTransition(current, hasFace, elapsed, timestampMs)
            is CompanionState.Tracking -> handleTrackingTransition(hasFace, timestampMs)
            is CompanionState.Searching -> handleSearchingTransition(current, hasFace, elapsed, timestampMs)
            is CompanionState.Poked -> handlePokedTransition(current, hasFace, elapsed, timestampMs)
        }
    }

    private fun handleIdleTransition(hasFace: Boolean, timestampMs: Long) {
        if (hasFace) {
            transitionTo(CompanionState.Awakening(0L), timestampMs)
        }
    }

    private fun handleAwakeningTransition(
        current: CompanionState.Awakening,
        hasFace: Boolean,
        elapsed: Long,
        timestampMs: Long
    ) {
        if (elapsed >= current.totalDurationMs) {
            val nextState = if (hasFace) CompanionState.Tracking else CompanionState.Searching(0L)
            transitionTo(nextState, timestampMs)
        } else {
            state = current.copy(elapsedMs = elapsed)
        }
    }

    private fun handleTrackingTransition(hasFace: Boolean, timestampMs: Long) {
        if (!hasFace) {
            transitionTo(CompanionState.Searching(0L), timestampMs)
        }
    }

    private fun handleSearchingTransition(
        current: CompanionState.Searching,
        hasFace: Boolean,
        elapsed: Long,
        timestampMs: Long
    ) {
        if (hasFace) {
            transitionTo(CompanionState.Awakening(0L), timestampMs)
        } else if (elapsed >= current.totalDurationMs) {
            transitionTo(CompanionState.Idle, timestampMs)
        } else {
            state = current.copy(elapsedMs = elapsed)
        }
    }

    private fun handlePokedTransition(
        current: CompanionState.Poked,
        hasFace: Boolean,
        elapsed: Long,
        timestampMs: Long
    ) {
        if (elapsed >= current.totalDurationMs) {
            val nextState = if (hasFace) {
                CompanionState.Tracking
            } else if (current.previousState is CompanionState.Searching) {
                CompanionState.Searching(0L)
            } else {
                CompanionState.Idle
            }
            transitionTo(nextState, timestampMs)
        } else {
            state = current.copy(elapsedMs = elapsed)
        }
    }

    private fun transitionTo(newState: CompanionState, timestampMs: Long) {
        state = newState
        stateEnterTimeMs = timestampMs
    }

    private fun computeOutput(faceGaze: GazeTarget, hasFace: Boolean): BehaviorOutput {
        return when (val current = state) {
            is CompanionState.Idle -> computeIdleOutput(current)
            is CompanionState.Awakening -> computeAwakeningOutput(current, faceGaze)
            is CompanionState.Tracking -> computeTrackingOutput(current, faceGaze)
            is CompanionState.Searching -> computeSearchingOutput(current)
            is CompanionState.Poked -> computePokedOutput(current, faceGaze, hasFace)
        }
    }

    private fun computeIdleOutput(current: CompanionState.Idle) = BehaviorOutput(
        state = current,
        targetGaze = GazeTarget.Center,
        eyelidModifier = DEFAULT_EYELID,
        pupilScaleModifier = DEFAULT_PUPIL_SCALE,
        awakeningProgress = ZERO_PROGRESS
    )

    private fun computeAwakeningOutput(current: CompanionState.Awakening, faceGaze: GazeTarget): BehaviorOutput {
        val progress = current.progress
        val expansion = (DEFAULT_PUPIL_SCALE + AWAKENING_PUPIL_EXPANSION * sin(PI.toFloat() * progress))
            .coerceAtLeast(DEFAULT_PUPIL_SCALE)
        return BehaviorOutput(
            state = current,
            targetGaze = faceGaze,
            eyelidModifier = AWAKENING_EYELID,
            pupilScaleModifier = expansion,
            awakeningProgress = progress
        )
    }

    private fun computeTrackingOutput(current: CompanionState.Tracking, faceGaze: GazeTarget) = BehaviorOutput(
        state = current,
        targetGaze = faceGaze,
        eyelidModifier = DEFAULT_EYELID,
        pupilScaleModifier = DEFAULT_PUPIL_SCALE,
        awakeningProgress = ZERO_PROGRESS
    )

    private fun computeSearchingOutput(current: CompanionState.Searching): BehaviorOutput {
        val elapsedSec = current.elapsedMs.toDouble() / SECONDS_DIVISOR
        val sweepX = (sin(elapsedSec * TWO_PI * SEARCHING_SWEEP_FREQ) * SEARCHING_SWEEP_AMPLITUDE)
            .toFloat().coerceIn(-1.0f, 1.0f)
        return BehaviorOutput(
            state = current,
            targetGaze = GazeTarget(sweepX, 0.0f),
            eyelidModifier = SEARCHING_EYELID,
            pupilScaleModifier = DEFAULT_PUPIL_SCALE,
            awakeningProgress = ZERO_PROGRESS
        )
    }

    private fun computePokedOutput(
        current: CompanionState.Poked,
        faceGaze: GazeTarget,
        hasFace: Boolean
    ): BehaviorOutput {
        val progress = current.progress
        val squintCurve = if (progress <= MID_PROGRESS) {
            POKED_MIN_EYELID
        } else {
            val factor = ((progress - MID_PROGRESS) / MID_PROGRESS).coerceIn(ZERO_PROGRESS, FULL_PROGRESS)
            POKED_MIN_EYELID + POKED_EYELID_RANGE * factor
        }
        return BehaviorOutput(
            state = current,
            targetGaze = if (hasFace) faceGaze else lastTargetGaze,
            eyelidModifier = squintCurve,
            pupilScaleModifier = POKED_PUPIL_SCALE,
            awakeningProgress = ZERO_PROGRESS
        )
    }
}
