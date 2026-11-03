package com.eyekage.core.model

/**
 * Macroscopic hand gestures recognized by EyeKage on-device vision pipeline.
 */
enum class HandGesture {
    NONE,
    WAVE,
    PEACE_SIGN,
    THUMBS_UP,
    CLOSED_FIST,
    FLAT_PALM_STOP
}

/**
 * User-configurable gesture detection sensitivity thresholds.
 *
 * @property minConfidence Minimum detector confidence required to evaluate candidate gesture.
 * @property confirmationWindowMs Time duration candidate gesture must remain continuous to trigger.
 */
enum class GestureSensitivity(
    val minConfidence: Float,
    val confirmationWindowMs: Long
) {
    LOW(minConfidence = 0.75f, confirmationWindowMs = 250L),
    NORMAL(minConfidence = 0.60f, confirmationWindowMs = 150L),
    HIGH(minConfidence = 0.50f, confirmationWindowMs = 100L);

    companion object {
        fun fromString(value: String): GestureSensitivity = when (value.uppercase()) {
            "LOW" -> LOW
            "HIGH" -> HIGH
            else -> NORMAL
        }
    }
}
