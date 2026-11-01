package com.eyekage.core.behavior

import com.eyekage.core.model.HeadPose
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import com.eyekage.core.model.ProximityZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HeadPoseReactionEngineTest {

    private lateinit var engine: HeadPoseReactionEngine

    @Before
    fun setUp() {
        engine = HeadPoseReactionEngine()
    }

    private fun createObservation(
        pitch: Float = 0f,
        yaw: Float = 0f,
        roll: Float = 0f
    ): PostureObservation {
        return PostureObservation(
            headPose = HeadPose(pitchDegrees = pitch, yawDegrees = yaw, rollDegrees = roll),
            proximity = ProximityZone.NORMAL,
            estimatedDistanceRatio = 0.20f,
            postureState = PostureState.BALANCED
        )
    }

    @Test
    fun `nominal head pose produces Idle gesture`() {
        val gesture = engine.processObservation(createObservation(pitch = 2f, yaw = -1f, roll = 3f))
        assertEquals(HeadPoseGesture.Idle, gesture)
    }

    @Test
    fun `lateral roll exceeding 12 degrees triggers InquisitiveTilt with hysteresis`() {
        // Roll reaches 14 degrees
        val gesture1 = engine.processObservation(createObservation(roll = 14f))
        assertTrue("Should be InquisitiveTilt", gesture1 is HeadPoseGesture.InquisitiveTilt)
        assertEquals(14f, (gesture1 as HeadPoseGesture.InquisitiveTilt).eyeTiltDegrees, 0.001f)

        // Roll decreases to 9 degrees (above reset threshold of 7 degrees) -> tilt should persist
        val gesture2 = engine.processObservation(createObservation(roll = 9f))
        assertTrue("Should still tilt due to hysteresis", gesture2 is HeadPoseGesture.InquisitiveTilt)

        // Roll drops to 5 degrees (below reset threshold) -> returns to Idle
        val gesture3 = engine.processObservation(createObservation(roll = 5f))
        assertEquals(HeadPoseGesture.Idle, gesture3)
    }

    @Test
    fun `reduced motion disables InquisitiveTilt`() {
        val gesture = engine.processObservation(
            observation = createObservation(roll = 20f),
            reducedMotion = true
        )
        assertEquals(HeadPoseGesture.Idle, gesture)
    }

    @Test
    fun `pitch dip and return within window triggers AffirmativeNod`() {
        var time = 1000L

        // 1. Initial pitch dip to -15 degrees (looking down)
        val g1 = engine.processObservation(createObservation(pitch = -15f), currentTimeMs = time)
        assertEquals(HeadPoseGesture.Idle, g1)

        // 2. Head returns up to 0 degrees 400ms later
        time += 400L
        val g2 = engine.processObservation(createObservation(pitch = 0f), currentTimeMs = time)
        assertEquals(HeadPoseGesture.AffirmativeNod, g2)
    }

    @Test
    fun `nod is not triggered if pitch return exceeds maximum window`() {
        var time = 1000L

        // Pitch dip
        engine.processObservation(createObservation(pitch = -16f), currentTimeMs = time)

        // Return 1500ms later (exceeds 1200ms window)
        time += 1500L
        val gesture = engine.processObservation(createObservation(pitch = 0f), currentTimeMs = time)
        assertEquals(HeadPoseGesture.Idle, gesture)
    }

    @Test
    fun `horizontal yaw exceeding 25 degrees triggers TurnedAway gesture`() {
        val gesture = engine.processObservation(createObservation(yaw = 30f))
        assertEquals(HeadPoseGesture.TurnedAway, gesture)
    }
}
