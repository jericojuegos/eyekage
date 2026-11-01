package com.eyekage.core.behavior

import com.eyekage.core.model.HeadPose
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import kotlin.math.abs

/**
 * Reactions triggered by dynamic head pose gestures.
 */
sealed interface HeadPoseGesture {
    data class InquisitiveTilt(val eyeTiltDegrees: Float) : HeadPoseGesture
    data object AffirmativeNod : HeadPoseGesture
    data object TurnedAway : HeadPoseGesture
    data object Idle : HeadPoseGesture
}

/**
 * Pure-Kotlin gesture recognizer and reaction generator for 3D head pose movements.
 *
 * Evaluates:
 * - Curious head tilt (roll $> 12^\circ$ with hysteresis).
 * - Affirmative nod cycles (pitch down dip $\le -12^\circ$ followed by return $\ge -4^\circ$ within 1.2s).
 * - Turned-away glance pauses ($|\text{yaw}| \ge 25^\circ$).
 */
class HeadPoseReactionEngine {

    companion object {
        const val NOD_DIP_THRESHOLD_DEGREES = -12.0f
        const val NOD_RETURN_THRESHOLD_DEGREES = -4.0f
        const val NOD_MAX_WINDOW_MS = 1200L
        const val NOD_MIN_WINDOW_MS = 150L
        const val NOD_COOLDOWN_MS = 1500L

        const val TILT_TRIGGER_THRESHOLD_DEGREES = 12.0f
        const val TILT_RESET_THRESHOLD_DEGREES = 7.0f
        const val MAX_EYE_TILT_DEGREES = 15.0f

        const val YAW_TURNED_AWAY_DEGREES = 25.0f
    }

    private var nodDipStartTimeMs: Long = 0L
    private var lastNodTriggeredMs: Long = 0L

    private var isCurrentlyTilting: Boolean = false
    private var currentEyeTiltDegrees: Float = 0f

    /**
     * Process an incoming [PostureObservation] and return the active [HeadPoseGesture].
     *
     * @param observation The current posture observation.
     * @param currentTimeMs Current system epoch or monotonic timestamp in milliseconds.
     * @param reducedMotion If true, suppresses physical eye tilt.
     */
    fun processObservation(
        observation: PostureObservation,
        currentTimeMs: Long = System.currentTimeMillis(),
        reducedMotion: Boolean = false
    ): HeadPoseGesture {
        val pose = observation.headPose

        // 1. Check Nod Gesture Detection (Pitch dip and return)
        val nodGesture = checkNodGesture(pose.pitchDegrees, currentTimeMs)
        if (nodGesture != null) {
            return nodGesture
        }

        // 2. Check Turned-Away State (Yaw)
        if (abs(pose.yawDegrees) >= YAW_TURNED_AWAY_DEGREES) {
            return HeadPoseGesture.TurnedAway
        }

        // 3. Check Lateral Curious Tilt (Roll)
        if (!reducedMotion) {
            val tiltGesture = checkTiltGesture(pose.rollDegrees)
            if (tiltGesture != null) {
                return tiltGesture
            }
        }

        return HeadPoseGesture.Idle
    }

    private fun checkNodGesture(pitchDegrees: Float, currentTimeMs: Long): HeadPoseGesture? {
        if (pitchDegrees <= NOD_DIP_THRESHOLD_DEGREES) {
            if (nodDipStartTimeMs == 0L && (lastNodTriggeredMs == 0L || currentTimeMs - lastNodTriggeredMs >= NOD_COOLDOWN_MS)) {
                nodDipStartTimeMs = currentTimeMs
            }
        } else if (pitchDegrees >= NOD_RETURN_THRESHOLD_DEGREES && nodDipStartTimeMs != 0L) {
            val elapsed = currentTimeMs - nodDipStartTimeMs
            nodDipStartTimeMs = 0L
            if (elapsed in NOD_MIN_WINDOW_MS..NOD_MAX_WINDOW_MS) {
                lastNodTriggeredMs = currentTimeMs
                return HeadPoseGesture.AffirmativeNod
            }
        } else if (nodDipStartTimeMs != 0L && (currentTimeMs - nodDipStartTimeMs > NOD_MAX_WINDOW_MS)) {
            // Window expired
            nodDipStartTimeMs = 0L
        }
        return null
    }

    private fun checkTiltGesture(rollDegrees: Float): HeadPoseGesture? {
        val absRoll = abs(rollDegrees)
        if (!isCurrentlyTilting) {
            if (absRoll >= TILT_TRIGGER_THRESHOLD_DEGREES) {
                isCurrentlyTilting = true
                currentEyeTiltDegrees = rollDegrees.coerceIn(-MAX_EYE_TILT_DEGREES, MAX_EYE_TILT_DEGREES)
                return HeadPoseGesture.InquisitiveTilt(currentEyeTiltDegrees)
            }
        } else {
            if (absRoll < TILT_RESET_THRESHOLD_DEGREES) {
                isCurrentlyTilting = false
                currentEyeTiltDegrees = 0f
            } else {
                currentEyeTiltDegrees = rollDegrees.coerceIn(-MAX_EYE_TILT_DEGREES, MAX_EYE_TILT_DEGREES)
                return HeadPoseGesture.InquisitiveTilt(currentEyeTiltDegrees)
            }
        }
        return null
    }

    fun reset() {
        nodDipStartTimeMs = 0L
        lastNodTriggeredMs = 0L
        isCurrentlyTilting = false
        currentEyeTiltDegrees = 0f
    }
}
