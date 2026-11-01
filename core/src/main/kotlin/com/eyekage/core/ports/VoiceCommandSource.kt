package com.eyekage.core.ports

import com.eyekage.core.model.VoiceCommandEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Output port contract for real-time on-device voice command observation.
 *
 * Implemented by platform speech recognizer drivers in `:platform:audio` or
 * mock implementations in test harnesses.
 */
interface VoiceCommandSource {
    /** Stream of speech recognition lifecycle and command events. */
    val events: Flow<VoiceCommandEvent>

    /** Observable state indicating whether the microphone is actively capturing audio. */
    val isListening: StateFlow<Boolean>

    /** Starts capturing microphone audio for on-device phrase recognition. */
    fun startListening()

    /** Ceases microphone audio capture and releases hardware resources. */
    fun stopListening()
}
