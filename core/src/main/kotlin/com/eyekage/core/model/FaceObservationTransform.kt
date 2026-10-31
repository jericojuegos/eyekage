package com.eyekage.core.model

/**
 * Coordinate mapping and transformation utilities for face observations.
 *
 * Translates raw image-space normalized coordinates [0.0, 1.0] to EyeKage's
 * centered normalized gaze space [-1.0, 1.0], accounting for camera sensor rotation
 * and front-camera horizontal mirroring.
 *
 * Pure Kotlin — zero Android SDK dependencies.
 */
object FaceObservationTransform {

    private const val COORDINATE_CENTER_OFFSET = 0.5f
    private const val COORDINATE_SCALE_FACTOR = 2.0f

    /**
     * Map raw normalized center coordinates [rawCenterX], [rawCenterY] in [0.0, 1.0]
     * to centered gaze space [-1.0, 1.0].
     *
     * @param rawCenterX Raw horizontal center in [0.0, 1.0].
     * @param rawCenterY Raw vertical center in [0.0, 1.0].
     * @param rotationDegrees Sensor rotation degrees (0, 90, 180, 270).
     * @param isFrontCamera Whether the source camera is front-facing (applies horizontal mirroring).
     * @return Pair of (normalizedX, normalizedY) in [-1.0, 1.0].
     */
    fun mapCoordinates(
        rawCenterX: Float,
        rawCenterY: Float,
        rotationDegrees: Int = 0,
        isFrontCamera: Boolean = true
    ): Pair<Float, Float> {
        // Step 1: Apply rotation transformation in [0.0, 1.0] space
        val (rotatedX, rotatedY) = when (normalizeRotation(rotationDegrees)) {
            90 -> Pair(1.0f - rawCenterY, rawCenterX)
            180 -> Pair(1.0f - rawCenterX, 1.0f - rawCenterY)
            270 -> Pair(rawCenterY, 1.0f - rawCenterX)
            else -> Pair(rawCenterX, rawCenterY)
        }

        // Step 2: Front camera horizontal mirroring (so looking left moves left from user's perspective)
        val mirroredX = if (isFrontCamera) 1.0f - rotatedX else rotatedX
        val finalY = rotatedY

        // Step 3: Scale from [0.0, 1.0] to [-1.0, 1.0] with center at (0.0, 0.0)
        val normX = ((mirroredX - COORDINATE_CENTER_OFFSET) * COORDINATE_SCALE_FACTOR).coerceIn(-1.0f, 1.0f)
        val normY = ((finalY - COORDINATE_CENTER_OFFSET) * COORDINATE_SCALE_FACTOR).coerceIn(-1.0f, 1.0f)

        return Pair(normX, normY)
    }

    /**
     * Maps raw bounding box dimensions to normalized size.
     */
    fun mapDimensions(
        rawWidth: Float,
        rawHeight: Float,
        rotationDegrees: Int = 0
    ): Pair<Float, Float> {
        return when (normalizeRotation(rotationDegrees)) {
            90, 270 -> Pair(
                (rawHeight * COORDINATE_SCALE_FACTOR).coerceIn(0.0f, 2.0f),
                (rawWidth * COORDINATE_SCALE_FACTOR).coerceIn(0.0f, 2.0f)
            )
            else -> Pair(
                (rawWidth * COORDINATE_SCALE_FACTOR).coerceIn(0.0f, 2.0f),
                (rawHeight * COORDINATE_SCALE_FACTOR).coerceIn(0.0f, 2.0f)
            )
        }
    }

    private fun normalizeRotation(rotationDegrees: Int): Int {
        val normalized = rotationDegrees % 360
        return if (normalized < 0) normalized + 360 else normalized
    }
}
