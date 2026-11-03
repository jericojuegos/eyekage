package com.eyekage.core.model

/**
 * Procedural behavioral eye reactions triggered by recognized hand gestures.
 */
sealed interface HandGestureReaction {
    /**
     * Cheerful wave greeting reaction: eyes widen, happy crescent squint, double blink.
     */
    data class WaveGreeting(val timestampMs: Long) : HandGestureReaction

    /**
     * Asymmetrical playful wink: one eye winks while other remains attentive.
     */
    data class PeaceWink(val isLeftEye: Boolean = true, val timestampMs: Long) : HandGestureReaction

    /**
     * Affirmative thumbs up approval: pupil bounce and affirmative nod cycle.
     */
    data class ThumbsUpApproval(val timestampMs: Long) : HandGestureReaction

    /**
     * Intense fist resolve: pupil scale constriction and focused eyebrow furrow.
     */
    data class FistResolve(val timestampMs: Long) : HandGestureReaction

    /**
     * Flat palm hold / pause: pauses procedural saccades and holds gaze in place.
     */
    data class HoldStop(val isFrozen: Boolean) : HandGestureReaction

    /**
     * Neutral companion behavior state.
     */
    data object None : HandGestureReaction
}
