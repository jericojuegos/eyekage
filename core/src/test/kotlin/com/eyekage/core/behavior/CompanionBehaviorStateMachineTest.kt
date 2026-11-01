package com.eyekage.core.behavior

import com.eyekage.core.model.FaceObservation
import com.eyekage.core.motion.EyeMotionEngine
import com.eyekage.core.ports.FakeFaceObservationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionBehaviorStateMachineTest {

    @Test
    fun `initial state is Idle with center gaze`() {
        val sm = CompanionBehaviorStateMachine()
        assertEquals(CompanionState.Idle, sm.currentState)

        val output = sm.update(FaceObservation.Empty, timestampMs = 1000L)
        assertEquals(CompanionState.Idle, output.state)
        assertEquals(0f, output.targetGaze.x, 0.001f)
        assertEquals(0f, output.targetGaze.y, 0.001f)
        assertEquals(1.0f, output.eyelidModifier, 0.001f)
        assertEquals(1.0f, output.pupilScaleModifier, 0.001f)
        assertEquals(0.0f, output.awakeningProgress, 0.001f)
    }

    @Test
    fun `face detection in Idle triggers Awakening reaction for 800 ms`() {
        val sm = CompanionBehaviorStateMachine()
        val detected = FaceObservation(normalizedX = 0.5f, normalizedY = -0.2f, confidence = 0.85f)

        // T = 1000ms: Face appears
        val out1 = sm.update(detected, timestampMs = 1000L)
        assertTrue(out1.state is CompanionState.Awakening)
        val awakening1 = out1.state as CompanionState.Awakening
        assertEquals(0L, awakening1.elapsedMs)
        assertEquals(0.0f, out1.awakeningProgress, 0.001f)
        assertEquals(0.5f, out1.targetGaze.x, 0.001f)
        assertEquals(-0.2f, out1.targetGaze.y, 0.001f)

        // T = 1400ms: Mid-Awakening (400ms elapsed)
        val out2 = sm.update(detected, timestampMs = 1400L)
        assertTrue(out2.state is CompanionState.Awakening)
        val awakening2 = out2.state as CompanionState.Awakening
        assertEquals(400L, awakening2.elapsedMs)
        assertEquals(0.5f, out2.awakeningProgress, 0.01f)
        assertTrue("Pupils should dilate during awakening", out2.pupilScaleModifier > 1.0f)

        // T = 1800ms: Awakening completed (800ms elapsed) -> transitions to Tracking
        val out3 = sm.update(detected, timestampMs = 1800L)
        assertEquals(CompanionState.Tracking, out3.state)
        assertEquals(0.0f, out3.awakeningProgress, 0.001f)
        assertEquals(1.0f, out3.pupilScaleModifier, 0.001f)
    }

    @Test
    fun `face lost during Awakening transitions to Searching`() {
        val sm = CompanionBehaviorStateMachine()
        val detected = FaceObservation(normalizedX = 0.3f, normalizedY = 0.1f, confidence = 0.9f)

        sm.update(detected, timestampMs = 1000L)
        assertTrue(sm.currentState is CompanionState.Awakening)

        // Face lost before awakening finishes, evaluate at 800ms timeout
        val out = sm.update(FaceObservation.Empty, timestampMs = 1800L)
        assertTrue(out.state is CompanionState.Searching)
        val searching = out.state as CompanionState.Searching
        assertEquals(0L, searching.elapsedMs)
    }

    @Test
    fun `losing face in Tracking triggers Searching saccades for 2000 ms before returning to Idle`() {
        val sm = CompanionBehaviorStateMachine()
        val detected = FaceObservation(normalizedX = -0.4f, normalizedY = 0.3f, confidence = 0.9f)

        // Transition: Idle -> Awakening -> Tracking
        sm.update(detected, timestampMs = 1000L)
        sm.update(detected, timestampMs = 1800L)
        assertEquals(CompanionState.Tracking, sm.currentState)

        // Face lost at T = 2000ms
        val outLost = sm.update(FaceObservation.Empty, timestampMs = 2000L)
        assertTrue(outLost.state is CompanionState.Searching)
        val searchingLost = outLost.state as CompanionState.Searching
        assertEquals(0L, searchingLost.elapsedMs)

        // T = 3000ms (1000ms into searching): Still searching with scanning saccades
        val outMid = sm.update(FaceObservation.Empty, timestampMs = 3000L)
        assertTrue(outMid.state is CompanionState.Searching)
        val searchingMid = outMid.state as CompanionState.Searching
        assertEquals(1000L, searchingMid.elapsedMs)
        assertEquals(0.5f, searchingMid.progress, 0.01f)

        // T = 4000ms (2000ms timeout reached): Returns to Idle
        val outIdle = sm.update(FaceObservation.Empty, timestampMs = 4000L)
        assertEquals(CompanionState.Idle, outIdle.state)
        assertEquals(0f, outIdle.targetGaze.x, 0.001f)
        assertEquals(0f, outIdle.targetGaze.y, 0.001f)
    }

    @Test
    fun `re-detecting face during Searching triggers Awakening immediately`() {
        val sm = CompanionBehaviorStateMachine()
        val detected = FaceObservation(normalizedX = 0.2f, normalizedY = 0.2f, confidence = 0.8f)

        sm.update(detected, timestampMs = 1000L)
        sm.update(detected, timestampMs = 1800L)
        assertEquals(CompanionState.Tracking, sm.currentState)

        // Face lost
        sm.update(FaceObservation.Empty, timestampMs = 2000L)
        assertTrue(sm.currentState is CompanionState.Searching)

        // Face re-appears at T = 2500ms
        val outReappear = sm.update(detected, timestampMs = 2500L)
        assertTrue(outReappear.state is CompanionState.Awakening)
        val awakening = outReappear.state as CompanionState.Awakening
        assertEquals(0L, awakening.elapsedMs)
    }

    @Test
    fun `screen poke triggers startled squint reaction for 400 ms and restores state`() {
        val sm = CompanionBehaviorStateMachine()
        val detected = FaceObservation(normalizedX = 0.2f, normalizedY = 0.2f, confidence = 0.8f)

        // In Tracking state
        sm.update(detected, timestampMs = 1000L)
        sm.update(detected, timestampMs = 1800L)
        assertEquals(CompanionState.Tracking, sm.currentState)

        // Poke at T = 2000ms
        sm.poke(timestampMs = 2000L)
        val outPokeStart = sm.update(detected, timestampMs = 2000L)
        assertTrue(outPokeStart.state is CompanionState.Poked)
        assertTrue("Eyelid should squint on poke start", outPokeStart.eyelidModifier < 0.6f)

        // Mid-poke (T = 2200ms, 200ms elapsed) -> maximum squint
        val outPokeMid = sm.update(detected, timestampMs = 2200L)
        assertTrue(outPokeMid.state is CompanionState.Poked)
        assertEquals(0.15f, outPokeMid.eyelidModifier, 0.05f)
        assertEquals(0.85f, outPokeMid.pupilScaleModifier, 0.001f)

        // End of poke (T = 2400ms, 400ms elapsed) -> restores to Tracking
        val outPokeEnd = sm.update(detected, timestampMs = 2400L)
        assertEquals(CompanionState.Tracking, outPokeEnd.state)
        assertEquals(1.0f, outPokeEnd.eyelidModifier, 0.001f)
    }

    @Test
    fun `poke works camera-independently in Idle state`() {
        val sm = CompanionBehaviorStateMachine()
        assertEquals(CompanionState.Idle, sm.currentState)

        sm.poke(timestampMs = 500L)
        val outPoke = sm.update(FaceObservation.Empty, timestampMs = 500L)
        assertTrue(outPoke.state is CompanionState.Poked)

        // Finishes and returns to Idle
        val outDone = sm.update(FaceObservation.Empty, timestampMs = 900L)
        assertEquals(CompanionState.Idle, outDone.state)
    }

    @Test
    fun `motion engine integrates dynamic FaceObservation with One-Euro smoothing`() {
        val motionEngine = EyeMotionEngine()
        val fakeSource = FakeFaceObservationSource()

        fakeSource.start()
        fakeSource.emitCoordinates(0.6f, -0.4f, confidence = 0.95f)
        val obs1 = fakeSource.observations.value

        // Frame 1
        val state1 = motionEngine.update(obs1, timestampMs = 1000L)
        assertEquals(0.6f, state1.gazeX, 0.05f)
        assertEquals(-0.4f, state1.gazeY, 0.05f)

        // Frame 2: sudden small noise step
        fakeSource.emitCoordinates(0.62f, -0.38f, confidence = 0.95f)
        val obs2 = fakeSource.observations.value
        val state2 = motionEngine.update(obs2, timestampMs = 1016L)

        // Filter should damp the high-frequency jitter
        assertTrue(state2.gazeX in 0.58f..0.63f)
        assertTrue(state2.gazeY in -0.42f..-0.37f)
    }
}
