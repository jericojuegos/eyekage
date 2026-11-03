package com.eyekage.core.behavior

import com.eyekage.core.model.GestureSensitivity
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandGestureReaction
import com.eyekage.core.model.HandObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HandGestureReactionEngineTest {

    private lateinit var engine: HandGestureReactionEngine

    @Before
    fun setUp() {
        engine = HandGestureReactionEngine(sensitivity = GestureSensitivity.NORMAL)
    }

    private fun obs(
        gesture: HandGesture,
        confidence: Float = 0.85f,
        x: Float? = 0f,
        y: Float? = 0f
    ) = HandObservation(
        gesture = gesture,
        confidence = confidence,
        normalizedCenterX = x,
        normalizedCenterY = y,
        boundingBoxArea = 0.15f
    )

    @Test
    fun `empty or None observation produces None reaction`() {
        val reaction = engine.processObservation(HandObservation.Empty, currentTimeMs = 1000L)
        assertEquals(HandGestureReaction.None, reaction)
    }

    @Test
    fun `transient gesture below confirmation window produces None`() {
        // First observation at t = 1000ms
        val r1 = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1000L)
        assertEquals(HandGestureReaction.None, r1)

        // Second observation at t = 1100ms (held for 100ms, window is 150ms)
        val r2 = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1100L)
        assertEquals(HandGestureReaction.None, r2)
    }

    @Test
    fun `stable wave gesture meeting confirmation window triggers WaveGreeting`() {
        engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1000L)
        val reaction = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1150L)

        assertTrue("Should trigger WaveGreeting", reaction is HandGestureReaction.WaveGreeting)
        assertEquals(1150L, (reaction as HandGestureReaction.WaveGreeting).timestampMs)
    }

    @Test
    fun `peace sign gesture triggers PeaceWink with eye side mapping`() {
        // Left hand side (x = -0.4f)
        engine.processObservation(obs(HandGesture.PEACE_SIGN, x = -0.4f), currentTimeMs = 2000L)
        val rLeft = engine.processObservation(obs(HandGesture.PEACE_SIGN, x = -0.4f), currentTimeMs = 2160L)

        assertTrue(rLeft is HandGestureReaction.PeaceWink)
        assertTrue("Negative x should wink left eye", (rLeft as HandGestureReaction.PeaceWink).isLeftEye)

        // Reset and test right side (x = 0.5f) after cooldown
        engine.reset()
        engine.processObservation(obs(HandGesture.PEACE_SIGN, x = 0.5f), currentTimeMs = 4000L)
        val rRight = engine.processObservation(obs(HandGesture.PEACE_SIGN, x = 0.5f), currentTimeMs = 4160L)

        assertTrue(rRight is HandGestureReaction.PeaceWink)
        assertFalse("Positive x should wink right eye", (rRight as HandGestureReaction.PeaceWink).isLeftEye)
    }

    @Test
    fun `thumbs up gesture triggers ThumbsUpApproval`() {
        engine.processObservation(obs(HandGesture.THUMBS_UP), currentTimeMs = 5000L)
        val reaction = engine.processObservation(obs(HandGesture.THUMBS_UP), currentTimeMs = 5150L)

        assertTrue(reaction is HandGestureReaction.ThumbsUpApproval)
        assertEquals(5150L, (reaction as HandGestureReaction.ThumbsUpApproval).timestampMs)
    }

    @Test
    fun `closed fist gesture triggers FistResolve`() {
        engine.processObservation(obs(HandGesture.CLOSED_FIST), currentTimeMs = 6000L)
        val reaction = engine.processObservation(obs(HandGesture.CLOSED_FIST), currentTimeMs = 6150L)

        assertTrue(reaction is HandGestureReaction.FistResolve)
        assertEquals(6150L, (reaction as HandGestureReaction.FistResolve).timestampMs)
    }

    @Test
    fun `flat palm stop triggers HoldStop with freeze and releases on hand removal`() {
        engine.processObservation(obs(HandGesture.FLAT_PALM_STOP), currentTimeMs = 7000L)
        val rHold = engine.processObservation(obs(HandGesture.FLAT_PALM_STOP), currentTimeMs = 7150L)

        assertTrue(rHold is HandGestureReaction.HoldStop)
        assertTrue((rHold as HandGestureReaction.HoldStop).isFrozen)

        // Hand removed -> releases freeze
        val rReleased = engine.processObservation(obs(HandGesture.NONE), currentTimeMs = 7300L)
        assertEquals(HandGestureReaction.None, rReleased)
    }

    @Test
    fun `flat palm stop unfreezes after max continuous hold duration`() {
        engine.processObservation(obs(HandGesture.FLAT_PALM_STOP), currentTimeMs = 8000L)
        val r1 = engine.processObservation(obs(HandGesture.FLAT_PALM_STOP), currentTimeMs = 8200L)
        assertTrue((r1 as HandGestureReaction.HoldStop).isFrozen)

        // After max duration of 2000ms: t = 8200 + 2100 = 10300ms
        val rMax = engine.processObservation(obs(HandGesture.FLAT_PALM_STOP), currentTimeMs = 10300L)
        assertTrue(rMax is HandGestureReaction.HoldStop)
        assertFalse("Hold freeze should unfreeze after max duration", (rMax as HandGestureReaction.HoldStop).isFrozen)
    }

    @Test
    fun `cooldown suppresses rapid repetitive triggers of same gesture`() {
        // First wave fires at t = 1150ms
        engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1000L)
        val r1 = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1150L)
        assertTrue(r1 is HandGestureReaction.WaveGreeting)

        // Continue waving at t = 1500ms (within 1500ms cooldown) -> returns active reaction, does not re-trigger
        val r2 = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1500L)
        assertEquals(r1, r2)

        // Reaction expires at t = 1150 + 1200 = 2350ms, but cooldown is 1500ms (until t = 2650ms)
        val r3 = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 2400L)
        assertEquals(HandGestureReaction.None, r3)

        // After cooldown expires at t = 2700ms -> can trigger again
        engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 2700L)
        val r4 = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 2860L)
        assertTrue("Should re-trigger after cooldown", r4 is HandGestureReaction.WaveGreeting)
    }

    @Test
    fun `confidence below active sensitivity threshold is ignored`() {
        engine.sensitivity = GestureSensitivity.LOW // requires confidence >= 0.75f, 250ms hold

        // Confidence 0.65f is below LOW threshold
        engine.processObservation(obs(HandGesture.WAVE, confidence = 0.65f), currentTimeMs = 1000L)
        val reaction = engine.processObservation(obs(HandGesture.WAVE, confidence = 0.65f), currentTimeMs = 1300L)
        assertEquals(HandGestureReaction.None, reaction)
    }

    @Test
    fun `high sensitivity triggers with lower confidence and shorter confirmation window`() {
        engine.sensitivity = GestureSensitivity.HIGH // confidence >= 0.50f, 100ms hold

        engine.processObservation(obs(HandGesture.WAVE, confidence = 0.55f), currentTimeMs = 1000L)
        val reaction = engine.processObservation(obs(HandGesture.WAVE, confidence = 0.55f), currentTimeMs = 1110L)
        assertTrue(reaction is HandGestureReaction.WaveGreeting)
    }

    @Test
    fun `disabled gesture recognition returns None and resets state`() {
        engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1000L, isEnabled = true)
        val rDisabled = engine.processObservation(obs(HandGesture.WAVE), currentTimeMs = 1200L, isEnabled = false)
        assertEquals(HandGestureReaction.None, rDisabled)
    }

    @Test
    fun `active reaction decays to None after duration expires`() {
        engine.processObservation(obs(HandGesture.PEACE_SIGN), currentTimeMs = 1000L)
        val r1 = engine.processObservation(obs(HandGesture.PEACE_SIGN), currentTimeMs = 1160L)
        assertTrue(r1 is HandGestureReaction.PeaceWink)

        // Hand drops: at t = 1800ms (wink duration is 600ms, expired at 1760ms)
        val rDecayed = engine.processObservation(obs(HandGesture.NONE), currentTimeMs = 1800L)
        assertEquals(HandGestureReaction.None, rDecayed)
    }
}
