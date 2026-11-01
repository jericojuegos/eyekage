package com.eyekage.platform.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import com.eyekage.core.behavior.VoiceCommandParser
import com.eyekage.core.model.VoiceCommandEvent
import com.eyekage.core.ports.VoiceCommandSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "SpeechRecognizerVoice"

/**
 * On-device Android platform speech recognizer implementation of [VoiceCommandSource].
 *
 * Captures user speech locally via Android's built-in [SpeechRecognizer] with offline preferences
 * ([RecognizerIntent.EXTRA_PREFER_OFFLINE] = true). Strips away network and cloud requirements.
 * Raw audio is never saved or buffered to disk. Emits parsed domain events via [VoiceCommandParser].
 */
@Suppress("TooGenericExceptionCaught")
class SpeechRecognizerVoiceSource(
    private val context: Context
) : VoiceCommandSource {

    private val _events = MutableSharedFlow<VoiceCommandEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<VoiceCommandEvent> = _events.asSharedFlow()

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isDesiredActive = false

    private val recognitionIntent: Intent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
            _isListening.value = true
            _events.tryEmit(VoiceCommandEvent.ListeningStarted)
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Decibel levels available for optional audio meter; not stored
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Explicitly no-op: raw audio is never recorded, saved, or persisted
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            val isPermissionError = error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "RECORD_AUDIO permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network error (offline mode required)"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Speech recognition error ($error)"
            }
            Log.w(TAG, "SpeechRecognizer error: $errorMessage")

            if (isPermissionError) {
                isDesiredActive = false
                _isListening.value = false
                _events.tryEmit(VoiceCommandEvent.Error(errorMessage, isPermissionError = true))
                _events.tryEmit(VoiceCommandEvent.ListeningStopped)
            } else {
                val isTimeoutOrNoMatch = error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                if (isDesiredActive && isTimeoutOrNoMatch) {
                    restartListening()
                } else {
                    _events.tryEmit(VoiceCommandEvent.Error(errorMessage))
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            handleCandidatePhrases(matches)
            if (isDesiredActive) {
                restartListening()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            handleCandidatePhrases(matches)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Reserved for vendor-specific recognition events
        }
    }

    override fun startListening() {
        mainHandler.post {
            val hasAudioPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasAudioPermission) {
                Log.w(TAG, "RECORD_AUDIO permission not granted")
                _events.tryEmit(
                    VoiceCommandEvent.Error(
                        "RECORD_AUDIO permission not granted",
                        isPermissionError = true
                    )
                )
                return@post
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "SpeechRecognizer is not available on this device")
                _events.tryEmit(VoiceCommandEvent.Error("Speech recognition not available on device"))
                return@post
            }

            isDesiredActive = true
            ensureRecognizerInitialized()
            try {
                speechRecognizer?.startListening(recognitionIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SpeechRecognizer", e)
                _events.tryEmit(VoiceCommandEvent.Error(e.message ?: "Failed to start recognition"))
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            isDesiredActive = false
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop SpeechRecognizer", e)
            } finally {
                _isListening.value = false
                _events.tryEmit(VoiceCommandEvent.ListeningStopped)
            }
        }
    }

    private fun restartListening() {
        mainHandler.post {
            if (!isDesiredActive) return@post
            try {
                speechRecognizer?.startListening(recognitionIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restart SpeechRecognizer", e)
            }
        }
    }

    private fun ensureRecognizerInitialized() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }
    }

    private fun handleCandidatePhrases(candidates: List<String>?) {
        if (candidates.isNullOrEmpty()) return
        for (phrase in candidates) {
            val command = VoiceCommandParser.parse(phrase)
            if (command != null) {
                Log.d(TAG, "Recognized command: $command from phrase: $phrase")
                _events.tryEmit(VoiceCommandEvent.CommandRecognized(command, phrase))
                break
            }
        }
    }
}
