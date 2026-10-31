package com.eyekage.core.model

/**
 * Normalized face observation emitted from on-device vision pipeline.
 *
 * Coordinates are normalized:
 * - [normalizedX]: [-1.0, 1.0] where 0.0 is center, -1.0 is far left, 1.0 is far right.
 * - [normalizedY]: [-1.0, 1.0] where 0.0 is center, -1.0 is top, 1.0 is bottom.
 * - [confidence]: [0.0, 1.0] detection confidence score.
 * - [timestampNanos]: Timestamp of the observed frame in nanoseconds.
 */
data class FaceObservation(
    val normalizedX: Float,
    val normalizedY: Float,
    val confidence: Float,
    val timestampNanos: Long = System.nanoTime()
) {
    init {
        require(confidence in 0f..1f) { "Confidence must be between 0.0 and 1.0, but was $confidence" }
    }

    companion object {
        val Empty = FaceObservation(
            normalizedX = 0f,
            normalizedY = 0f,
            confidence = 0f,
            timestampNanos = 0L
        )
    }
}
