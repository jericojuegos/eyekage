package com.eyekage.core.ports

import com.eyekage.core.model.VoiceCommand
import com.eyekage.core.model.VoiceCommandEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test double implementation of [VoiceCommandSource] allowing deterministic
 * emission of speech events and recognition simulation in unit tests.
 */
class FakeVoiceCommandSource : VoiceCommandSource {

    private val _events = MutableSharedFlow<VoiceCommandEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<VoiceCommandEvent> = _events.asSharedFlow()

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    var startListeningCallCount = 0
        private set

    var stopListeningCallCount = 0
        private set

    override fun startListening() {
        startListeningCallCount++
        _isListening.value = true
        _events.tryEmit(VoiceCommandEvent.ListeningStarted)
    }

    override fun stopListening() {
        stopListeningCallCount++
        _isListening.value = false
        _events.tryEmit(VoiceCommandEvent.ListeningStopped)
    }

    /** Helper for unit tests to simulate a recognized voice command. */
    fun emitCommand(command: VoiceCommand, rawPhrase: String = "") {
        _events.tryEmit(VoiceCommandEvent.CommandRecognized(command, rawPhrase))
    }

    /** Helper for unit tests to simulate a voice recognition error. */
    fun emitError(message: String, isPermissionError: Boolean = false) {
        _events.tryEmit(VoiceCommandEvent.Error(message, isPermissionError))
    }
}
