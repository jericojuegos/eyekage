package com.eyekage.core.ports

import com.eyekage.core.model.FaceObservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Scriptable test double for [FaceObservationSource].
 * Used in unit tests and automated CI verification to simulate face movements
 * without camera hardware or ML models.
 */
class FakeFaceObservationSource(
    initialObservation: FaceObservation = FaceObservation.Empty,
    initialIsRunning: Boolean = false
) : FaceObservationSource {

    private val _observations = MutableStateFlow(initialObservation)
    override val observations: StateFlow<FaceObservation> = _observations.asStateFlow()

    private val _isRunning = MutableStateFlow(initialIsRunning)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    /**
     * Emits a new [FaceObservation] to all active collectors.
     */
    fun emitObservation(observation: FaceObservation) {
        _observations.value = observation
    }

    /**
     * Emits a face observation at specific normalized coordinates.
     */
    fun emitCoordinates(x: Float, y: Float, confidence: Float = 0.9f) {
        _observations.value = FaceObservation(
            normalizedX = x.coerceIn(-1.0f, 1.0f),
            normalizedY = y.coerceIn(-1.0f, 1.0f),
            confidence = confidence.coerceIn(0.0f, 1.0f)
        )
    }

    /**
     * Clears the current observation to [FaceObservation.Empty].
     */
    fun clearObservation() {
        _observations.value = FaceObservation.Empty
    }

    override fun start() {
        _isRunning.value = true
    }

    override fun stop() {
        _isRunning.value = false
    }
}
