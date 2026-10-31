package com.eyekage.core.ports

import com.eyekage.core.model.FaceObservation
import kotlinx.coroutines.flow.StateFlow

/**
 * Port interface for emitting real-time normalized face observations from an on-device detector.
 *
 * Implementations live strictly in `:platform` and must never cross image buffers into `:core`.
 */
interface FaceObservationSource {
    /**
     * Observable stream of the latest normalized face observation.
     */
    val observations: StateFlow<FaceObservation>

    /**
     * Whether the detector pipeline is actively processing frames.
     */
    val isRunning: StateFlow<Boolean>

    /**
     * Starts the observation pipeline.
     */
    fun start()

    /**
     * Stops the observation pipeline and releases any active resources.
     */
    fun stop()
}
