package com.eyekage.core.behavior

import com.eyekage.core.motion.GazeTarget

/**
 * High-level behavior states for the EyeKage companion robot.
 */
sealed interface CompanionState {

    /**
     * Ambient idle state when no face is currently observed.
     * Generates spontaneous natural blinks and micro-saccades.
     */
    data object Idle : CompanionState

    /**
     * Transient reaction state triggered when a face is newly detected from [Idle] or [Searching].
     * Drives an amber radial energy burst for [totalDurationMs] (default 800 ms).
     */
    data class Awakening(
        val elapsedMs: Long,
        val totalDurationMs: Long = AWAKENING_DURATION_MS
    ) : CompanionState {
        val progress: Float
            get() = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0.0f, 1.0f)
    }

    /**
     * Active gaze-tracking state locking onto detected facial coordinates.
     */
    data object Tracking : CompanionState

    /**
     * Searching state triggered when an actively tracked face is lost.
     * Executes exploratory scanning saccades for [totalDurationMs] (default 2000 ms)
     * before transitioning back to [Idle].
     */
    data class Searching(
        val elapsedMs: Long,
        val totalDurationMs: Long = SEARCHING_DURATION_MS
    ) : CompanionState {
        val progress: Float
            get() = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0.0f, 1.0f)
    }

    /**
     * Playful startled squint reaction triggered immediately by screen/eye poke or tap.
     * Narrowed eyelids and slight pupil retraction for [totalDurationMs] (default 400 ms),
     * operating identically whether camera is active or disabled.
     */
    data class Poked(
        val elapsedMs: Long,
        val totalDurationMs: Long = POKED_DURATION_MS,
        val previousState: CompanionState = Idle
    ) : CompanionState {
        val progress: Float
            get() = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0.0f, 1.0f)
    }

    companion object {
        const val AWAKENING_DURATION_MS = 800L
        const val SEARCHING_DURATION_MS = 2000L
        const val POKED_DURATION_MS = 400L
    }
}

/**
 * Output emitted by [CompanionBehaviorStateMachine] on each evaluation frame.
 *
 * @param state Current behavioral state.
 * @param targetGaze Target gaze coordinates for the motion engine.
 * @param eyelidModifier Multiplier applied to baseline eyelid openness (e.g. squinting during poke).
 * @param pupilScaleModifier Multiplier applied to baseline pupil size (e.g. widening during awakening).
 * @param awakeningProgress Progress of the awakening reaction [0.0f, 1.0f].
 */
data class BehaviorOutput(
    val state: CompanionState,
    val targetGaze: GazeTarget,
    val eyelidModifier: Float = 1.0f,
    val pupilScaleModifier: Float = 1.0f,
    val awakeningProgress: Float = 0.0f
)
