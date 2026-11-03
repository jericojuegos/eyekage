package com.eyekage.core.ports

import com.eyekage.core.model.HandObservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test double for [HandGestureSource] supporting deterministic emissions.
 */
class FakeHandGestureSource(
    initialObservation: HandObservation = HandObservation.Empty
) : HandGestureSource {

    private val _handGestures = MutableStateFlow(initialObservation)
    override val handGestures: StateFlow<HandObservation> = _handGestures.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    fun emit(observation: HandObservation) {
        _handGestures.value = observation
    }

    override fun start() {
        _isTracking.value = true
    }

    override fun stop() {
        _isTracking.value = false
        _handGestures.value = HandObservation.Empty
    }
}
