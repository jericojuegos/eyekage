package com.eyekage.core.model

/**
 * 3D head orientation represented by Euler angles in degrees.
 *
 * All angles are in degrees:
 * - [pitchDegrees]: Vertical tilt / nod. Negative is looking down (slouch/nod), positive is looking up.
 * - [yawDegrees]: Horizontal turn. Negative is turned left, positive is turned right.
 * - [rollDegrees]: Lateral tilt. Negative is tilted left, positive is tilted right.
 * - [timestampNanos]: Frame capture timestamp in nanoseconds.
 */
data class HeadPose(
    val pitchDegrees: Float,
    val yawDegrees: Float,
    val rollDegrees: Float,
    val timestampNanos: Long = System.nanoTime()
) {
    val isNominal: Boolean
        get() = pitchDegrees in -15f..15f && yawDegrees in -25f..25f && rollDegrees in -12f..12f

    companion object {
        val Neutral = HeadPose(
            pitchDegrees = 0f,
            yawDegrees = 0f,
            rollDegrees = 0f,
            timestampNanos = 0L
        )
    }
}
