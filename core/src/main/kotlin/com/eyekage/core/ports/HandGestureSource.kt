package com.eyekage.core.ports

import com.eyekage.core.model.HandObservation
import kotlinx.coroutines.flow.StateFlow

/**
 * Port interface for emitting real-time hand gesture observations.
 *
 * Implementations live in `:platform` and compute gesture events from camera frames,
 * never passing raw images or biometric identity data across module boundaries.
 */
interface HandGestureSource {
    /**
     * Observable stream of the latest hand gesture observation.
     */
    val handGestures: StateFlow<HandObservation>

    /**
     * Whether hand gesture tracking is actively running.
     */
    val isTracking: StateFlow<Boolean>

    /**
     * Starts tracking hand gestures.
     */
    fun start()

    /**
     * Stops tracking and releases resources.
     */
    fun stop()
}
