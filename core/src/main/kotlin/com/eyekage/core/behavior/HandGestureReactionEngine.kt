package com.eyekage.core.behavior

import com.eyekage.core.model.GestureSensitivity
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandGestureReaction
import com.eyekage.core.model.HandObservation

/**
 * Pure-Kotlin state machine and reaction generator for macroscopic hand gestures.
 *
 * Implements:
 * - Configurable temporal confirmation window (100–250 ms) to suppress transient candidate transitions.
 * - Minimum re-trigger cooldown (1500 ms) to prevent event spam.
 * - Dynamic sensitivity filtering based on [GestureSensitivity].
 * - Natural decay of procedural reactions back to neutral companion state.
 */
class HandGestureReactionEngine(
    var sensitivity: GestureSensitivity = GestureSensitivity.NORMAL
) {
    companion object {
        const val DEFAULT_COOLDOWN_MS = 1500L
        const val WAVE_REACTION_DURATION_MS = 1200L
        const val WINK_REACTION_DURATION_MS = 600L
        const val THUMBS_UP_REACTION_DURATION_MS = 1200L
        const val FIST_REACTION_DURATION_MS = 1200L
        const val MAX_HOLD_FREEZE_DURATION_MS = 2000L
    }

    private var candidateGesture: HandGesture = HandGesture.NONE
    private var candidateStartTimeMs: Long = 0L

    private val lastTriggeredTimeMs = mutableMapOf<HandGesture, Long>()

    private var activeReaction: HandGestureReaction = HandGestureReaction.None
    private var activeReactionExpiryMs: Long = 0L
    private var holdStopStartTimeMs: Long = 0L

    /**
     * Process an incoming [HandObservation] and return the active [HandGestureReaction].
     *
     * @param observation The current hand observation from vision.
     * @param currentTimeMs Current timestamp in milliseconds.
     * @param isEnabled Whether gesture recognition is enabled in preferences.
     */
    fun processObservation(
        observation: HandObservation,
        currentTimeMs: Long = System.currentTimeMillis(),
        isEnabled: Boolean = true
    ): HandGestureReaction {
        if (!isEnabled) {
            reset()
            return HandGestureReaction.None
        }

        // 1. Decay or expire active non-hold reaction
        if (activeReaction != HandGestureReaction.None && activeReaction !is HandGestureReaction.HoldStop) {
            if (currentTimeMs >= activeReactionExpiryMs) {
                activeReaction = HandGestureReaction.None
            }
        }

        val gesture = observation.gesture
        val confidence = observation.confidence

        // 2. Validate against active sensitivity threshold
        if (gesture == HandGesture.NONE || confidence < sensitivity.minConfidence) {
            candidateGesture = HandGesture.NONE
            candidateStartTimeMs = 0L

            if (activeReaction is HandGestureReaction.HoldStop) {
                activeReaction = HandGestureReaction.None
                holdStopStartTimeMs = 0L
            }
            return activeReaction
        }

        // 3. Track candidate gesture stability
        if (gesture != candidateGesture) {
            candidateGesture = gesture
            candidateStartTimeMs = currentTimeMs
        }

        val heldDuration = currentTimeMs - candidateStartTimeMs

        // 4. Check confirmation window threshold
        if (heldDuration >= sensitivity.confirmationWindowMs) {
            if (gesture == HandGesture.FLAT_PALM_STOP) {
                if (holdStopStartTimeMs == 0L) {
                    holdStopStartTimeMs = currentTimeMs
                }
                val freezeElapsed = currentTimeMs - holdStopStartTimeMs
                activeReaction = if (freezeElapsed <= MAX_HOLD_FREEZE_DURATION_MS) {
                    HandGestureReaction.HoldStop(isFrozen = true)
                } else {
                    HandGestureReaction.HoldStop(isFrozen = false)
                }
                return activeReaction
            } else {
                // Discrete gestures require cooldown
                val lastTriggered = lastTriggeredTimeMs[gesture]
                if (lastTriggered == null || currentTimeMs - lastTriggered >= DEFAULT_COOLDOWN_MS) {
                    lastTriggeredTimeMs[gesture] = currentTimeMs
                    val reaction = createReactionForGesture(gesture, observation, currentTimeMs)
                    activeReaction = reaction
                    activeReactionExpiryMs = currentTimeMs + getDurationForReaction(reaction)
                    return reaction
                }
            }
        }

        return activeReaction
    }

    private fun createReactionForGesture(
        gesture: HandGesture,
        observation: HandObservation,
        currentTimeMs: Long
    ): HandGestureReaction = when (gesture) {
        HandGesture.WAVE -> HandGestureReaction.WaveGreeting(currentTimeMs)
        HandGesture.PEACE_SIGN -> {
            val isLeftEye = (observation.normalizedCenterX ?: 0f) <= 0f
            HandGestureReaction.PeaceWink(isLeftEye = isLeftEye, timestampMs = currentTimeMs)
        }
        HandGesture.THUMBS_UP -> HandGestureReaction.ThumbsUpApproval(currentTimeMs)
        HandGesture.CLOSED_FIST -> HandGestureReaction.FistResolve(currentTimeMs)
        HandGesture.FLAT_PALM_STOP -> HandGestureReaction.HoldStop(isFrozen = true)
        HandGesture.NONE -> HandGestureReaction.None
    }

    private fun getDurationForReaction(reaction: HandGestureReaction): Long = when (reaction) {
        is HandGestureReaction.WaveGreeting -> WAVE_REACTION_DURATION_MS
        is HandGestureReaction.PeaceWink -> WINK_REACTION_DURATION_MS
        is HandGestureReaction.ThumbsUpApproval -> THUMBS_UP_REACTION_DURATION_MS
        is HandGestureReaction.FistResolve -> FIST_REACTION_DURATION_MS
        is HandGestureReaction.HoldStop -> MAX_HOLD_FREEZE_DURATION_MS
        is HandGestureReaction.None -> 0L
    }

    /**
     * Resets all candidate tracking and cooldown state.
     */
    fun reset() {
        candidateGesture = HandGesture.NONE
        candidateStartTimeMs = 0L
        lastTriggeredTimeMs.clear()
        activeReaction = HandGestureReaction.None
        activeReactionExpiryMs = 0L
        holdStopStartTimeMs = 0L
    }
}
