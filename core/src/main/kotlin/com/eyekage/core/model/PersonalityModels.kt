package com.eyekage.core.model

/**
 * High-level mood state for the companion robot.
 */
enum class MoodState {
    CURIOUS,
    PLAYFUL,
    ALERT,
    DROWSY,
    RESTING
}

private const val MIN_DIMENSION = 0.0f
private const val MAX_DIMENSION = 1.0f

/**
 * Three continuous emotional dimensions modeling companion personality in [0.0, 1.0].
 *
 * @param curiosity Drive to explore and inspect surroundings / faces.
 * @param playfulness Drive to react excitably to touch pokes.
 * @param energy Stamina / alertness level decaying over long periods of inactivity.
 */
data class PersonalityVector(
    val curiosity: Float = 0.5f,
    val playfulness: Float = 0.5f,
    val energy: Float = 1.0f
) {
    init {
        require(curiosity in MIN_DIMENSION..MAX_DIMENSION) { "curiosity must be in [0.0, 1.0]" }
        require(playfulness in MIN_DIMENSION..MAX_DIMENSION) { "playfulness must be in [0.0, 1.0]" }
        require(energy in MIN_DIMENSION..MAX_DIMENSION) { "energy must be in [0.0, 1.0]" }
    }
}

/**
 * Procedural parameters applied to eye rendering representing subtle facial micro-expressions.
 *
 * @param eyelidOpennessMultiplier Multiplier for upper eyelid openness (e.g., droop when tired).
 * @param bottomEyelidArch Upward curvature for bottom eyelid (0.0 = flat, 1.0 = happy crescent).
 * @param eyeTiltDegrees Asymmetrical tilt in degrees for curious glance.
 * @param saccadeSpeedMultiplier Saccade velocity multiplier.
 */
data class MicroExpression(
    val eyelidOpennessMultiplier: Float = 1.0f,
    val bottomEyelidArch: Float = 0.0f,
    val eyeTiltDegrees: Float = 0.0f,
    val saccadeSpeedMultiplier: Float = 1.0f
)

/**
 * Immutable snapshot of accumulated companion metrics persisted across app launches.
 *
 * @param totalPokes Cumulative user eye touch/poke interactions.
 * @param totalGazeSeconds Cumulative seconds of active face gaze tracking.
 * @param bondTier Progression level from 1 to 5.
 * @param lastInteractionTimestamp Epoch milliseconds of the most recent interaction.
 */
data class CompanionMemory(
    val totalPokes: Int = 0,
    val totalGazeSeconds: Long = 0L,
    val bondTier: Int = 1,
    val lastInteractionTimestamp: Long = 0L
)
