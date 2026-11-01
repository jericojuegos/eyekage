package com.eyekage.core.behavior

import com.eyekage.core.model.MoodState
import com.eyekage.core.model.PersonalityVector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PersonalityEngineTest {

    private lateinit var engine: PersonalityEngine

    @Before
    fun setUp() {
        engine = PersonalityEngine()
    }

    @Test
    fun `initial vector has expected default values`() {
        val vector = engine.vector
        assertEquals(0.5f, vector.curiosity, 0.001f)
        assertEquals(0.5f, vector.playfulness, 0.001f)
        assertEquals(1.0f, vector.energy, 0.001f)
    }

    @Test
    fun `curiosity increases when face is tracked and caps at 1_0`() {
        // 10 seconds of face tracking -> +0.5 curiosity
        engine.update(deltaSeconds = 10.0f, isFaceTracked = true)
        assertEquals(1.0f, engine.vector.curiosity, 0.001f)
        assertEquals(MoodState.CURIOUS, engine.currentMood)

        // Further tracking does not exceed 1.0
        engine.update(deltaSeconds = 5.0f, isFaceTracked = true)
        assertEquals(1.0f, engine.vector.curiosity, 0.001f)
    }

    @Test
    fun `curiosity decays when face is not tracked down to minimum 0_2`() {
        engine.update(deltaSeconds = 30.0f, isFaceTracked = false)
        assertEquals(0.2f, engine.vector.curiosity, 0.001f)
    }

    @Test
    fun `poke increases playfulness and triggers happy arch micro-expression`() {
        engine.reset(newPlayfulness = 0.5f)
        engine.onPoke()
        assertEquals(0.7f, engine.vector.playfulness, 0.001f)
        assertEquals(MoodState.PLAYFUL, engine.currentMood)

        val micro = engine.computeMicroExpression()
        assertTrue("Arch should be > 0.0 when playful", micro.bottomEyelidArch > 0.0f)
    }

    @Test
    fun `inactivity for over 120 seconds decays energy into drowsy mood`() {
        // 120 seconds of inactivity -> no energy decay yet
        engine.update(deltaSeconds = 120.0f, isFaceTracked = false)
        assertEquals(1.0f, engine.vector.energy, 0.001f)

        // Additional 65 seconds -> energy decays 0.65 -> 0.35 <= 0.4 (DROWSY)
        engine.update(deltaSeconds = 65.0f, isFaceTracked = false)
        assertEquals(0.35f, engine.vector.energy, 0.005f)
        assertEquals(MoodState.DROWSY, engine.currentMood)

        val micro = engine.computeMicroExpression()
        assertEquals(0.55f, micro.eyelidOpennessMultiplier, 0.001f)
        assertEquals(0.60f, micro.saccadeSpeedMultiplier, 0.001f)
    }

    @Test
    fun `poke while drowsy snaps energy back to at least 0_85`() {
        engine.reset(newEnergy = 0.3f)
        assertEquals(MoodState.DROWSY, engine.currentMood)

        engine.onPoke()
        assertTrue("Energy should be restored to >= 0.85 on poke", engine.vector.energy >= 0.85f)
    }

    @Test
    fun `face acquisition while drowsy snaps energy back to at least 0_85`() {
        engine.reset(newEnergy = 0.25f)
        assertEquals(MoodState.DROWSY, engine.currentMood)

        engine.update(deltaSeconds = 0.1f, isFaceTracked = true)
        assertTrue("Energy should be restored to >= 0.85 on face acquisition", engine.vector.energy >= 0.85f)
    }

    @Test
    fun `curiosity at or above 0_7 triggers asymmetrical tilt unless reduced motion is enabled`() {
        engine.reset(newCuriosity = 0.85f)
        val normalMicro = engine.computeMicroExpression(reducedMotion = false)
        assertTrue("Tilt should be between 4 and 8 degrees", normalMicro.eyeTiltDegrees in 4.0f..8.0f)

        val reducedMicro = engine.computeMicroExpression(reducedMotion = true)
        assertEquals(0.0f, reducedMicro.eyeTiltDegrees, 0.001f)
    }

    @Test
    fun `bond tier advances according to gaze second thresholds`() {
        assertEquals(1, engine.computeBondTier(0L))
        assertEquals(1, engine.computeBondTier(299L))
        assertEquals(2, engine.computeBondTier(300L))
        assertEquals(2, engine.computeBondTier(899L))
        assertEquals(3, engine.computeBondTier(900L))
        assertEquals(3, engine.computeBondTier(1799L))
        assertEquals(4, engine.computeBondTier(1800L))
        assertEquals(4, engine.computeBondTier(3599L))
        assertEquals(5, engine.computeBondTier(3600L))
        assertEquals(5, engine.computeBondTier(10000L))
    }
}
