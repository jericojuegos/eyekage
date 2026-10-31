package com.eyekage.core.motion

import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

/**
 * Manages autonomous spontaneous blinking with realistic biological timing,
 * asymmetric closing/opening easing curves, and double-blink probability.
 *
 * @param random Source of randomness for scheduling intervals.
 * @param minIntervalSeconds Minimum idle time between blinks (default 3.0s).
 * @param maxIntervalSeconds Maximum idle time between blinks (default 7.0s).
 * @param blinkDurationMs Typical duration of a single blink in milliseconds (default 180ms).
 */
class BlinkScheduler(
    private val random: Random = Random.Default,
    val minIntervalSeconds: Float = DEFAULT_MIN_INTERVAL_SEC,
    val maxIntervalSeconds: Float = DEFAULT_MAX_INTERVAL_SEC,
    val blinkDurationMs: Long = DEFAULT_BLINK_DURATION_MS
) {
    var reducedMotion: Boolean = false

    private var nextBlinkTimeMs: Long = 0L
    private var blinkStartTimeMs: Long = 0L
    private var currentDurationMs: Long = blinkDurationMs
    private var isBlinking: Boolean = false
    private var isDoubleBlinkQueued: Boolean = false

    init {
        scheduleNextBlink(currentTimeMs = 0L)
    }

    /**
     * Resets scheduler state to [currentTimeMs] and schedules the next blink.
     */
    fun reset(currentTimeMs: Long = 0L) {
        isBlinking = false
        isDoubleBlinkQueued = false
        blinkStartTimeMs = 0L
        scheduleNextBlink(currentTimeMs)
    }

    /**
     * Manually triggers a blink immediately.
     */
    fun triggerBlink(currentTimeMs: Long) {
        startBlink(currentTimeMs, blinkDurationMs)
    }

    /**
     * Updates the scheduler and returns the current eyelid openness in [0.0f, 1.0f],
     * where 1.0f represents a fully open eye and 0.0f represents fully closed.
     */
    fun update(currentTimeMs: Long): Float {
        if (!isBlinking) {
            if (currentTimeMs >= nextBlinkTimeMs) {
                val shouldDoubleBlink = !reducedMotion && (random.nextFloat() < DOUBLE_BLINK_PROBABILITY)
                isDoubleBlinkQueued = shouldDoubleBlink
                startBlink(currentTimeMs, blinkDurationMs)
            } else {
                return 1.0f
            }
        }

        // Active blink in progress
        val elapsed = currentTimeMs - blinkStartTimeMs
        if (elapsed >= currentDurationMs) {
            isBlinking = false
            if (isDoubleBlinkQueued) {
                isDoubleBlinkQueued = false
                // Schedule second quick blink shortly after
                nextBlinkTimeMs = currentTimeMs + DOUBLE_BLINK_PAUSE_MS
            } else {
                scheduleNextBlink(currentTimeMs)
            }
            return 1.0f
        }

        if (reducedMotion) {
            // Soft-fade or quick snap for reduced motion
            val progress = elapsed.toFloat() / currentDurationMs.toFloat()
            return if (progress in 0.2f..0.8f) 0.1f else 1.0f
        }

        // Natural closing (faster ~40% of duration) and reopening (~60% of duration)
        val progress = elapsed.toFloat() / currentDurationMs.toFloat()
        return calculateEyelidCurve(progress)
    }

    private fun startBlink(currentTimeMs: Long, durationMs: Long) {
        isBlinking = true
        blinkStartTimeMs = currentTimeMs
        currentDurationMs = durationMs
    }

    private fun scheduleNextBlink(currentTimeMs: Long) {
        val intervalRange = maxIntervalSeconds - minIntervalSeconds
        val randomOffsetSec = minIntervalSeconds + random.nextFloat() * intervalRange
        nextBlinkTimeMs = currentTimeMs + (randomOffsetSec * 1000f).toLong()
    }

    private fun calculateEyelidCurve(progress: Float): Float {
        // Closing phase: 0.0 to 0.4
        // Opening phase: 0.4 to 1.0
        val closeThreshold = 0.4f
        return if (progress < closeThreshold) {
            val t = progress / closeThreshold
            // Easing from 1.0 to 0.0
            0.5f * (1.0f + cos(t * PI.toFloat()))
        } else {
            val t = (progress - closeThreshold) / (1.0f - closeThreshold)
            // Easing from 0.0 to 1.0
            0.5f * (1.0f - cos(t * PI.toFloat()))
        }
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_SEC = 3.0f
        const val DEFAULT_MAX_INTERVAL_SEC = 7.0f
        const val DEFAULT_BLINK_DURATION_MS = 180L
        const val DOUBLE_BLINK_PROBABILITY = 0.10f
        const val DOUBLE_BLINK_PAUSE_MS = 100L
    }
}
