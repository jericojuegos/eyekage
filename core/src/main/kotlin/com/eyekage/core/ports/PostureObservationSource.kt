package com.eyekage.core.ports

import com.eyekage.core.model.PostureObservation
import kotlinx.coroutines.flow.StateFlow

/**
 * Port interface for emitting real-time head pose and posture observations.
 *
 * Implementations live in `:platform` and compute posture from camera landmarks,
 * never passing raw camera frames across module boundaries.
 */
interface PostureObservationSource {
    /**
     * Observable stream of the latest posture observation.
     */
    val posture: StateFlow<PostureObservation>

    /**
     * Whether head pose tracking is actively running.
     */
    val isTracking: StateFlow<Boolean>

    /**
     * Starts tracking head pose and posture.
     */
    fun start()

    /**
     * Stops tracking and releases resources.
     */
    fun stop()
}
