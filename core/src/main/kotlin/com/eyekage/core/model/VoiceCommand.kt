package com.eyekage.core.model

/**
 * Pure Kotlin representation of discrete user voice command intents.
 *
 * Uses neutral identifiers and references [EyeModeId] to switch modes,
 * trigger reactions, or adjust companion state without Android dependencies.
 */
sealed interface VoiceCommand {
    /** Switch to a specific selectable eye mode. */
    data class SwitchMode(val modeId: EyeModeId) : VoiceCommand

    /** Trigger the Awakening reaction and mood alertness boost. */
    data object TriggerAwakening : VoiceCommand

    /** Transition the companion into resting or drowsy sleep state. */
    data object Sleep : VoiceCommand

    /** Trigger an immediate single procedural eyelid blink. */
    data object Blink : VoiceCommand

    /** Center the companion's pupil gaze coordinates. */
    data object CenterGaze : VoiceCommand
}
