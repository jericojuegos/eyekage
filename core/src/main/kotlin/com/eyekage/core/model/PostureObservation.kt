package com.eyekage.core.model

/**
 * Categorical distance / proximity classification relative to camera.
 */
enum class ProximityZone {
    TOO_CLOSE, // < 30 cm (eyeSpan > 0.35)
    NEAR,      // 30 - 45 cm (eyeSpan in 0.22 .. 0.35)
    NORMAL,    // 45 - 65 cm (eyeSpan in 0.12 .. 0.22)
    FAR        // > 65 cm (eyeSpan < 0.12)
}

/**
 * Real-time posture assessment state.
 */
enum class PostureState {
    BALANCED,
    SLOUCHING_DOWN,
    LEANING_CLOSE,
    TILTED_CURIOUS,
    TURNED_AWAY
}

/**
 * Real-time composite posture and head pose observation emitted by vision engine.
 */
data class PostureObservation(
    val headPose: HeadPose,
    val proximity: ProximityZone,
    val estimatedDistanceRatio: Float,
    val postureState: PostureState,
    val timestampNanos: Long = System.nanoTime()
) {
    val isSlouching: Boolean
        get() = postureState == PostureState.SLOUCHING_DOWN || postureState == PostureState.LEANING_CLOSE

    companion object {
        val Neutral = PostureObservation(
            headPose = HeadPose.Neutral,
            proximity = ProximityZone.NORMAL,
            estimatedDistanceRatio = 0.18f,
            postureState = PostureState.BALANCED,
            timestampNanos = 0L
        )
    }
}
