package com.eyekage.core.model

/**
 * Observable events emitted by the voice recognition pipeline.
 */
sealed interface VoiceCommandEvent {
    /** The speech recognition engine has started capturing audio hardware. */
    data object ListeningStarted : VoiceCommandEvent

    /** A recognized spoken phrase was successfully parsed into a domain [VoiceCommand]. */
    data class CommandRecognized(
        val command: VoiceCommand,
        val rawPhrase: String
    ) : VoiceCommandEvent

    /** The speech recognition engine has ceased audio hardware capture. */
    data object ListeningStopped : VoiceCommandEvent

    /** An error occurred in speech recognition or audio capture. */
    data class Error(
        val message: String,
        val isPermissionError: Boolean = false
    ) : VoiceCommandEvent
}
