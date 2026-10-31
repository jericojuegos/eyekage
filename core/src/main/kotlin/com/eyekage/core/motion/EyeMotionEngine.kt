package com.eyekage.core.motion

/**
 * Normalized 2D gaze coordinates bounded within [-1.0f, 1.0f].
 */
data class GazeTarget(
    val x: Float,
    val y: Float
) {
    init {
        require(x in -1.0f..1.0f) { "GazeTarget x must be in range [-1.0, 1.0], was $x" }
        require(y in -1.0f..1.0f) { "GazeTarget y must be in range [-1.0, 1.0], was $y" }
    }

    companion object {
        val Center = GazeTarget(0f, 0f)
    }
}

/**
 * Immutable snapshot of motion engine state at a single frame.
 *
 * @param gazeX Smoothed and saccade-adjusted horizontal pupil offset in [-1.0f, 1.0f].
 * @param gazeY Smoothed and saccade-adjusted vertical pupil offset in [-1.0f, 1.0f].
 * @param eyelidOpenness Factor in [0.0f, 1.0f] where 1.0 is fully open and 0.0 is fully closed.
 * @param pupilScale Pupil scale factor (default 1.0f).
 */
data class EyeMotionState(
    val gazeX: Float = 0f,
    val gazeY: Float = 0f,
    val eyelidOpenness: Float = 1.0f,
    val pupilScale: Float = 1.0f
)

/**
 * Unified motion engine driving gaze smoothing, spontaneous blinking,
 * and idle micro-saccades.
 */
class EyeMotionEngine(
    private val filter: OneEuroFilter2D = OneEuroFilter2D(),
    private val blinkScheduler: BlinkScheduler = BlinkScheduler(),
    private val saccadeGenerator: SaccadeGenerator = SaccadeGenerator()
) {
    var reducedMotion: Boolean
        get() = blinkScheduler.reducedMotion
        set(value) {
            blinkScheduler.reducedMotion = value
            saccadeGenerator.reducedMotion = value
        }

    fun reset(currentTimeMs: Long = 0L) {
        filter.reset()
        blinkScheduler.reset(currentTimeMs)
        saccadeGenerator.reset(currentTimeMs)
    }

    /**
     * Updates motion state for the given timestamp.
     *
     * @param target Normalized target coordinates [-1.0, 1.0].
     * @param timestampMs Frame timestamp in milliseconds.
     */
    fun update(
        target: GazeTarget = GazeTarget.Center,
        timestampMs: Long
    ): EyeMotionState {
        val timestampSec = timestampMs.toDouble() / 1000.0
        val (smoothedX, smoothedY) = filter.filter(target.x, target.y, timestampSec)
        val (saccadeX, saccadeY) = saccadeGenerator.update(timestampMs)
        val eyelid = blinkScheduler.update(timestampMs)

        val combinedX = (smoothedX + saccadeX).coerceIn(-1.0f, 1.0f)
        val combinedY = (smoothedY + saccadeY).coerceIn(-1.0f, 1.0f)

        return EyeMotionState(
            gazeX = combinedX,
            gazeY = combinedY,
            eyelidOpenness = eyelid,
            pupilScale = 1.0f
        )
    }
}
