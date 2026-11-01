package com.eyekage.core.ports

import com.eyekage.core.model.PostureObservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test double for [PostureObservationSource] supporting deterministic emissions.
 */
class FakePostureObservationSource(
    initialPosture: PostureObservation = PostureObservation.Neutral
) : PostureObservationSource {

    private val _posture = MutableStateFlow(initialPosture)
    override val posture: StateFlow<PostureObservation> = _posture.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    fun emit(observation: PostureObservation) {
        _posture.value = observation
    }

    override fun start() {
        _isTracking.value = true
    }

    override fun stop() {
        _isTracking.value = false
        _posture.value = PostureObservation.Neutral
    }
}
