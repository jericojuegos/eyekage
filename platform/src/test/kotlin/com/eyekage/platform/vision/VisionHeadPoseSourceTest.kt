package com.eyekage.platform.vision

import com.eyekage.core.model.FacialLandmarks
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import com.eyekage.core.model.ProximityZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VisionHeadPoseSourceTest {

    private lateinit var source: VisionHeadPoseSource

    @Before
    fun setUp() {
        source = VisionHeadPoseSource()
    }

    @Test
    fun `initial state is not tracking and has neutral posture`() = runTest {
        assertFalse(source.isTracking.first())
        val posture = source.posture.first()
        assertEquals(PostureState.BALANCED, posture.postureState)
        assertEquals(0f, posture.headPose.rollDegrees, 0.001f)
        assertEquals(0f, posture.headPose.pitchDegrees, 0.001f)
        assertEquals(0f, posture.headPose.yawDegrees, 0.001f)
    }

    @Test
    fun `start and stop manage tracking lifecycle and reset state`() = runTest {
        source.start()
        assertTrue(source.isTracking.first())

        source.stop()
        assertFalse(source.isTracking.first())
        assertEquals(PostureObservation.Neutral, source.posture.first())
    }

    @Test
    fun `processLandmarks computes and smooths posture observations`() = runTest {
        source.start()

        // Provide landmarks for curious tilt right
        val tiltLandmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.30f),
            rightEye = Pair(0.60f, 0.40f),
            noseTip = Pair(0.50f, 0.48f),
            mouthCenter = Pair(0.50f, 0.60f)
        )

        source.processLandmarks(tiltLandmarks, timestampNanos = 1_000_000_000L)
        val obs1 = source.posture.first()
        assertTrue("Roll should be positive for right tilt: ${obs1.headPose.rollDegrees}", obs1.headPose.rollDegrees > 5f)

        // Face lost resets to neutral
        source.onFaceLost()
        val lostObs = source.posture.first()
        assertEquals(PostureState.BALANCED, lostObs.postureState)
        assertEquals(0f, lostObs.headPose.rollDegrees, 0.001f)
    }

    @Test
    fun `slouching landmarks emit SLOUCHING_DOWN posture state`() = runTest {
        source.start()

        val slouchLandmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.35f),
            rightEye = Pair(0.60f, 0.35f),
            noseTip = Pair(0.50f, 0.58f), // nose drops far down
            mouthCenter = Pair(0.50f, 0.64f)
        )

        source.processLandmarks(slouchLandmarks, timestampNanos = 2_000_000_000L)
        val obs = source.posture.first()
        assertTrue("Pitch should be negative: ${obs.headPose.pitchDegrees}", obs.headPose.pitchDegrees < -10f)
        assertEquals(PostureState.SLOUCHING_DOWN, obs.postureState)
    }
}
