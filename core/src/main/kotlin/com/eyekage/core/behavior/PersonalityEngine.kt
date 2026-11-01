package com.eyekage.core.behavior

import com.eyekage.core.model.MicroExpression
import com.eyekage.core.model.MoodState
import com.eyekage.core.model.PersonalityVector

private const val MIN_VAL = 0.0f
private const val MAX_VAL = 1.0f

private const val DEFAULT_CURIOSITY = 0.5f
private const val DEFAULT_PLAYFULNESS = 0.5f
private const val DEFAULT_ENERGY = 1.0f

private const val CURIOSITY_GAIN_PER_SEC = 0.05f
private const val CURIOSITY_DECAY_PER_SEC = 0.02f
private const val CURIOSITY_MIN = 0.2f

private const val PLAYFULNESS_POKE_BOOST = 0.2f
private const val PLAYFULNESS_DECAY_PER_SEC = 0.02f

private const val INACTIVITY_THRESHOLD_SEC = 120.0f
private const val ENERGY_DECAY_PER_SEC = 0.01f
private const val MIN_ENERGY = 0.2f
private const val DROWSY_ENERGY_THRESHOLD = 0.4f
private const val AWAKEN_RESTORE_ENERGY = 0.85f

private const val PLAYFUL_ARCH_THRESHOLD = 0.6f
private const val PLAYFUL_ARCH_RANGE = 0.4f

private const val CURIOUS_TILT_THRESHOLD = 0.7f
private const val CURIOUS_TILT_RANGE = 0.3f
private const val MIN_TILT_DEG = 4.0f
private const val MAX_TILT_DELTA_DEG = 4.0f

private const val DROWSY_EYELID_OPENNESS = 0.55f
private const val DROWSY_SACCADE_SPEED = 0.60f
private const val PLAYFUL_SACCADE_BOOST = 0.5f

private const val TIER_2_SECONDS = 300L
private const val TIER_3_SECONDS = 900L
private const val TIER_4_SECONDS = 1800L
private const val TIER_5_SECONDS = 3600L

private const val BOND_TIER_1 = 1
private const val BOND_TIER_2 = 2
private const val BOND_TIER_3 = 3
private const val BOND_TIER_4 = 4
private const val BOND_TIER_5 = 5

/**
 * Pure-Kotlin deterministic personality engine driving companion emotional dynamics
 * and facial micro-expressions.
 */
class PersonalityEngine(
    initialVector: PersonalityVector = PersonalityVector(
        curiosity = DEFAULT_CURIOSITY,
        playfulness = DEFAULT_PLAYFULNESS,
        energy = DEFAULT_ENERGY
    )
) {
    private var curiosity = initialVector.curiosity
    private var playfulness = initialVector.playfulness
    private var energy = initialVector.energy

    private var secondsSinceInteraction = 0.0f

    val vector: PersonalityVector
        get() = PersonalityVector(
            curiosity = curiosity,
            playfulness = playfulness,
            energy = energy
        )

    val currentMood: MoodState
        get() = when {
            energy <= DROWSY_ENERGY_THRESHOLD -> MoodState.DROWSY
            playfulness >= PLAYFUL_ARCH_THRESHOLD -> MoodState.PLAYFUL
            curiosity >= CURIOUS_TILT_THRESHOLD -> MoodState.CURIOUS
            curiosity > CURIOSITY_MIN -> MoodState.ALERT
            else -> MoodState.RESTING
        }

    /**
     * Resets emotional dimensions to initial baseline.
     */
    fun reset(
        newCuriosity: Float = DEFAULT_CURIOSITY,
        newPlayfulness: Float = DEFAULT_PLAYFULNESS,
        newEnergy: Float = DEFAULT_ENERGY
    ) {
        curiosity = newCuriosity.coerceIn(MIN_VAL, MAX_VAL)
        playfulness = newPlayfulness.coerceIn(MIN_VAL, MAX_VAL)
        energy = newEnergy.coerceIn(MIN_VAL, MAX_VAL)
        secondsSinceInteraction = 0.0f
    }

    /**
     * Reacts to user touch/poke interaction on companion eyes.
     */
    fun onPoke() {
        val wasDrowsy = energy <= DROWSY_ENERGY_THRESHOLD
        if (wasDrowsy) {
            energy = energy.coerceAtLeast(AWAKEN_RESTORE_ENERGY)
        }
        playfulness = (playfulness + PLAYFULNESS_POKE_BOOST).coerceAtMost(MAX_VAL)
        secondsSinceInteraction = 0.0f
    }

    /**
     * Updates personality dimensions over elapsed frame time.
     *
     * @param deltaSeconds Time elapsed since last frame in seconds.
     * @param isFaceTracked Whether a face is actively tracked by the vision pipeline.
     */
    fun update(
        deltaSeconds: Float,
        isFaceTracked: Boolean
    ): PersonalityVector {
        if (deltaSeconds <= 0.0f) return vector

        if (isFaceTracked) {
            val wasDrowsy = energy <= DROWSY_ENERGY_THRESHOLD
            if (wasDrowsy) {
                energy = energy.coerceAtLeast(AWAKEN_RESTORE_ENERGY)
            }
            curiosity = (curiosity + CURIOSITY_GAIN_PER_SEC * deltaSeconds).coerceAtMost(MAX_VAL)
            secondsSinceInteraction = 0.0f
        } else {
            curiosity = (curiosity - CURIOSITY_DECAY_PER_SEC * deltaSeconds).coerceAtLeast(CURIOSITY_MIN)
            secondsSinceInteraction += deltaSeconds
        }

        playfulness = (playfulness - PLAYFULNESS_DECAY_PER_SEC * deltaSeconds).coerceAtLeast(MIN_VAL)

        if (secondsSinceInteraction > INACTIVITY_THRESHOLD_SEC) {
            energy = (energy - ENERGY_DECAY_PER_SEC * deltaSeconds).coerceAtLeast(MIN_ENERGY)
        }

        return vector
    }

    /**
     * Computes the eye micro-expression parameters based on current emotional vector.
     */
    fun computeMicroExpression(reducedMotion: Boolean = false): MicroExpression {
        val arch = if (playfulness >= PLAYFUL_ARCH_THRESHOLD) {
            ((playfulness - PLAYFUL_ARCH_THRESHOLD) / PLAYFUL_ARCH_RANGE).coerceIn(MIN_VAL, MAX_VAL)
        } else {
            MIN_VAL
        }

        val tilt = if (!reducedMotion && curiosity >= CURIOUS_TILT_THRESHOLD) {
            val factor = ((curiosity - CURIOUS_TILT_THRESHOLD) / CURIOUS_TILT_RANGE).coerceIn(MIN_VAL, MAX_VAL)
            MIN_TILT_DEG + MAX_TILT_DELTA_DEG * factor
        } else {
            MIN_VAL
        }

        val (eyelidOpenness, saccadeSpeed) = if (energy <= DROWSY_ENERGY_THRESHOLD) {
            DROWSY_EYELID_OPENNESS to DROWSY_SACCADE_SPEED
        } else {
            MAX_VAL to (MAX_VAL + PLAYFUL_SACCADE_BOOST * playfulness)
        }

        return MicroExpression(
            eyelidOpennessMultiplier = eyelidOpenness,
            bottomEyelidArch = arch,
            eyeTiltDegrees = tilt,
            saccadeSpeedMultiplier = saccadeSpeed
        )
    }

    /**
     * Determines companion bond tier (1 to 5) from cumulative face tracking seconds.
     */
    fun computeBondTier(totalGazeSeconds: Long): Int = when {
        totalGazeSeconds >= TIER_5_SECONDS -> BOND_TIER_5
        totalGazeSeconds >= TIER_4_SECONDS -> BOND_TIER_4
        totalGazeSeconds >= TIER_3_SECONDS -> BOND_TIER_3
        totalGazeSeconds >= TIER_2_SECONDS -> BOND_TIER_2
        else -> BOND_TIER_1
    }
}
