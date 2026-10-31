package com.eyekage.core.motion

import kotlin.random.Random

/**
 * Generates natural micro-saccades (subtle, involuntary fixation shifts of the eye)
 * to maintain autonomous liveliness during idle states.
 *
 * @param random Source of randomness for scheduling and target offsets.
 * @param minIntervalSec Minimum idle interval between saccades (default 1.5s).
 * @param maxIntervalSec Maximum idle interval between saccades (default 4.0s).
 * @param maxAmplitude Maximum normalized coordinate displacement (default 0.04f).
 */
class SaccadeGenerator(
    private val random: Random = Random.Default,
    val minIntervalSec: Float = DEFAULT_MIN_INTERVAL_SEC,
    val maxIntervalSec: Float = DEFAULT_MAX_INTERVAL_SEC,
    val maxAmplitude: Float = DEFAULT_MAX_AMPLITUDE
) {
    var reducedMotion: Boolean = false

    private var nextSaccadeTimeMs: Long = 0L
    private var currentOffsetX: Float = 0f
    private var currentOffsetY: Float = 0f

    init {
        scheduleNextSaccade(currentTimeMs = 0L)
    }

    fun reset(currentTimeMs: Long = 0L) {
        currentOffsetX = 0f
        currentOffsetY = 0f
        scheduleNextSaccade(currentTimeMs)
    }

    /**
     * Updates saccade state and returns the current normalized (x, y) micro-offset.
     */
    fun update(currentTimeMs: Long): Pair<Float, Float> {
        if (reducedMotion) {
            return Pair(0f, 0f)
        }

        if (currentTimeMs >= nextSaccadeTimeMs) {
            // Pick a new micro-saccade offset
            currentOffsetX = (random.nextFloat() * 2.0f - 1.0f) * maxAmplitude
            currentOffsetY = (random.nextFloat() * 2.0f - 1.0f) * maxAmplitude
            scheduleNextSaccade(currentTimeMs)
        }

        return Pair(currentOffsetX, currentOffsetY)
    }

    private fun scheduleNextSaccade(currentTimeMs: Long) {
        val range = maxIntervalSec - minIntervalSec
        val intervalSec = minIntervalSec + random.nextFloat() * range
        nextSaccadeTimeMs = currentTimeMs + (intervalSec * 1000f).toLong()
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_SEC = 1.5f
        const val DEFAULT_MAX_INTERVAL_SEC = 4.0f
        const val DEFAULT_MAX_AMPLITUDE = 0.04f
    }
}
