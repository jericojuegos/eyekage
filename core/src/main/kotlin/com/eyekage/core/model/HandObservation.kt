package com.eyekage.core.model

/**
 * Real-time observation of hand gesture activity from on-device vision pipeline.
 *
 * @property gesture Categorical recognized [HandGesture].
 * @property confidence Classification confidence score in [0.0, 1.0].
 * @property normalizedCenterX Normalized horizontal hand center in centered [-1.0, 1.0] coordinates.
 * @property normalizedCenterY Normalized vertical hand center in centered [-1.0, 1.0] coordinates.
 * @property boundingBoxArea Fraction of camera frame area occupied by the hand bounding box.
 * @property timestampNanos Capture timestamp in nanoseconds.
 */
data class HandObservation(
    val gesture: HandGesture,
    val confidence: Float,
    val normalizedCenterX: Float? = null,
    val normalizedCenterY: Float? = null,
    val boundingBoxArea: Float = 0f,
    val timestampNanos: Long = System.nanoTime()
) {
    val isGestureDetected: Boolean
        get() = gesture != HandGesture.NONE && confidence > 0f

    companion object {
        val Empty = HandObservation(
            gesture = HandGesture.NONE,
            confidence = 0f,
            normalizedCenterX = null,
            normalizedCenterY = null,
            boundingBoxArea = 0f,
            timestampNanos = 0L
        )
    }
}
