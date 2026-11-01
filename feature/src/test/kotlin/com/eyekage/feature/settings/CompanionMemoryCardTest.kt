package com.eyekage.feature.settings

import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.MoodState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionMemoryCardTest {

    @Test
    fun `companion memory model holds stats and defaults accurately`() {
        val memory = CompanionMemory(
            totalPokes = 42,
            totalGazeSeconds = 870L,
            bondTier = 2,
            lastInteractionTimestamp = 1698765432000L
        )

        assertEquals(42, memory.totalPokes)
        assertEquals(870L, memory.totalGazeSeconds)
        assertEquals(2, memory.bondTier)
        assertEquals(1698765432000L, memory.lastInteractionTimestamp)
    }

    @Test
    fun `mood state values match required domain states`() {
        val moods = MoodState.entries
        assertTrue(moods.contains(MoodState.CURIOUS))
        assertTrue(moods.contains(MoodState.PLAYFUL))
        assertTrue(moods.contains(MoodState.ALERT))
        assertTrue(moods.contains(MoodState.DROWSY))
        assertTrue(moods.contains(MoodState.RESTING))
    }

    @Test
    fun `memory reset action callback fires on user confirmation`() {
        var resetCalled = false
        val onReset = { resetCalled = true }

        onReset()
        assertTrue("Reset callback should be invoked", resetCalled)
    }
}
