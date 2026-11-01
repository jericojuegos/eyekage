package com.eyekage.core.ports

import com.eyekage.core.model.CompanionMemory
import kotlinx.coroutines.flow.StateFlow

/**
 * Port interface for reading, recording, and resetting persistent companion memory.
 */
interface MemoryRepository {

    /**
     * Flow emitting the current persistent memory state.
     */
    val memory: StateFlow<CompanionMemory>

    /**
     * Records a single user tap or poke on the companion's eye area.
     */
    suspend fun recordPoke()

    /**
     * Accumulates active face gaze tracking duration in seconds.
     */
    suspend fun recordGazeDuration(seconds: Long)

    /**
     * Atomically resets all memory metrics to initial default state.
     */
    suspend fun clearMemory()
}
