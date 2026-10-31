package com.eyekage.core.motion

import kotlin.math.PI
import kotlin.math.abs

/**
 * 1D Low-pass filter component for the 1€ (One-Euro) Filter.
 * Formulation based on Casiez, Roussel, and Vogel (CHI 2012).
 */
class LowPassFilter(private var alpha: Float = 1.0f) {
    private var lastValue: Float? = null

    fun filter(value: Float, alpha: Float): Float {
        this.alpha = alpha
        val prev = lastValue
        val result = if (prev == null) {
            value
        } else {
            alpha * value + (1.0f - alpha) * prev
        }
        lastValue = result
        return result
    }

    val last: Float? get() = lastValue

    fun reset() {
        lastValue = null
    }
}

/**
 * 1D One-Euro Filter implementation for adaptive jitter reduction and low-latency tracking.
 *
 * @param minCutoff Minimum cutoff frequency (fc_min) in Hz. Lower values reduce jitter when stationary.
 * @param beta Speed coefficient (beta). Higher values reduce lag during fast movements.
 * @param dCutoff Derivative cutoff frequency (fc_der) in Hz.
 */
class OneEuroFilter(
    val minCutoff: Float = DEFAULT_MIN_CUTOFF,
    val beta: Float = DEFAULT_BETA,
    val dCutoff: Float = DEFAULT_D_CUTOFF
) {
    private val xFilter = LowPassFilter()
    private val dxFilter = LowPassFilter()
    private var lastTimestampSeconds: Double? = null

    fun filter(value: Float, timestampSeconds: Double): Float {
        val lastTimestamp = lastTimestampSeconds
        if (lastTimestamp == null) {
            lastTimestampSeconds = timestampSeconds
            return xFilter.filter(value, 1.0f)
        }

        val dt = (timestampSeconds - lastTimestamp).toFloat().coerceAtLeast(MIN_DELTA_TIME)
        lastTimestampSeconds = timestampSeconds

        val prevX = xFilter.last ?: value
        val dx = (value - prevX) / dt
        val dAlpha = computeAlpha(dt, dCutoff)
        val smoothedDx = dxFilter.filter(dx, dAlpha)

        val cutoff = minCutoff + beta * abs(smoothedDx)
        val alpha = computeAlpha(dt, cutoff)

        return xFilter.filter(value, alpha)
    }

    fun reset() {
        xFilter.reset()
        dxFilter.reset()
        lastTimestampSeconds = null
    }

    private fun computeAlpha(dt: Float, cutoff: Float): Float {
        val tau = 1.0f / (2.0f * PI.toFloat() * cutoff)
        return 1.0f / (1.0f + tau / dt)
    }

    companion object {
        const val DEFAULT_MIN_CUTOFF = 1.0f
        const val DEFAULT_BETA = 0.007f
        const val DEFAULT_D_CUTOFF = 1.0f
        private const val MIN_DELTA_TIME = 1e-4f
    }
}

/**
 * 2D One-Euro Filter for normalized 2D coordinate pairs (e.g. gaze targets, facial landmarks).
 */
class OneEuroFilter2D(
    minCutoff: Float = OneEuroFilter.DEFAULT_MIN_CUTOFF,
    beta: Float = OneEuroFilter.DEFAULT_BETA,
    dCutoff: Float = OneEuroFilter.DEFAULT_D_CUTOFF
) {
    private val xFilter = OneEuroFilter(minCutoff, beta, dCutoff)
    private val yFilter = OneEuroFilter(minCutoff, beta, dCutoff)

    fun filter(x: Float, y: Float, timestampSeconds: Double): Pair<Float, Float> {
        val filteredX = xFilter.filter(x, timestampSeconds)
        val filteredY = yFilter.filter(y, timestampSeconds)
        return Pair(filteredX, filteredY)
    }

    fun reset() {
        xFilter.reset()
        yFilter.reset()
    }
}
