package com.eyekage.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadPoseTransformTest {

    @Test
    fun `nominal upright face produces balanced posture near zero degrees`() {
        // Standard upright landmarks: eye level at Y=0.35, nose at Y=0.50, mouth at Y=0.62
        // Interocular distance = 0.20 (centered at X=0.50)
        val landmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.35f),  // image space right eye -> in user space becomes left eye
            rightEye = Pair(0.60f, 0.35f), // image space left eye -> in user space becomes right eye
            noseTip = Pair(0.50f, 0.50f),
            mouthCenter = Pair(0.50f, 0.62f)
        )

        val obs = HeadPoseTransform.calculatePosture(landmarks, rotationDegrees = 0, isFrontCamera = true)

        assertEquals(PostureState.BALANCED, obs.postureState)
        assertEquals(ProximityZone.NORMAL, obs.proximity)
        assertTrue("Roll should be near zero", kotlin.math.abs(obs.headPose.rollDegrees) < 5.0f)
        assertTrue("Yaw should be near zero", kotlin.math.abs(obs.headPose.yawDegrees) < 5.0f)
        assertTrue("Pitch should be nominal", kotlin.math.abs(obs.headPose.pitchDegrees) < 10.0f)
        assertTrue(obs.headPose.isNominal)
        assertFalse(obs.isSlouching)
    }

    @Test
    fun `lateral head tilt triggers curious tilt and correct roll sign`() {
        // User tilts head to their right: right eye drops (higher Y), left eye rises (lower Y) in user space
        val tiltRightLandmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.30f),  // user left eye
            rightEye = Pair(0.60f, 0.40f), // user right eye
            noseTip = Pair(0.50f, 0.48f),
            mouthCenter = Pair(0.50f, 0.60f)
        )

        val obsRight = HeadPoseTransform.calculatePosture(tiltRightLandmarks)
        assertTrue("Roll should be positive for right tilt: ${obsRight.headPose.rollDegrees}", obsRight.headPose.rollDegrees > 12.0f)
        assertEquals(PostureState.TILTED_CURIOUS, obsRight.postureState)

        // User tilts head to their left: left eye drops (higher Y), right eye rises (lower Y) in user space
        val tiltLeftLandmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.40f),
            rightEye = Pair(0.60f, 0.30f),
            noseTip = Pair(0.50f, 0.48f),
            mouthCenter = Pair(0.50f, 0.60f)
        )

        val obsLeft = HeadPoseTransform.calculatePosture(tiltLeftLandmarks)
        assertTrue("Roll should be negative for left tilt: ${obsLeft.headPose.rollDegrees}", obsLeft.headPose.rollDegrees < -12.0f)
        assertEquals(PostureState.TILTED_CURIOUS, obsLeft.postureState)
    }

    @Test
    fun `head turned horizontally triggers turned away posture state`() {
        // User turns head to the right: nose shifts toward user right (lower X in user space, higher X in image space)
        val turnRightLandmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.35f),
            rightEye = Pair(0.60f, 0.35f),
            noseTip = Pair(0.38f, 0.50f),
            mouthCenter = Pair(0.40f, 0.62f)
        )

        val obsTurn = HeadPoseTransform.calculatePosture(turnRightLandmarks)
        assertTrue("Yaw magnitude should exceed 25 degrees: ${obsTurn.headPose.yawDegrees}", kotlin.math.abs(obsTurn.headPose.yawDegrees) >= 25.0f)
        assertEquals(PostureState.TURNED_AWAY, obsTurn.postureState)
    }

    @Test
    fun `downward head pitch triggers slouching down posture`() {
        // Looking down (slouching): nose drops closer to mouth relative to eyeline
        val slouchLandmarks = FacialLandmarks(
            leftEye = Pair(0.40f, 0.35f),
            rightEye = Pair(0.60f, 0.35f),
            noseTip = Pair(0.50f, 0.58f), // nose drops far down
            mouthCenter = Pair(0.50f, 0.64f)
        )

        val obs = HeadPoseTransform.calculatePosture(slouchLandmarks)
        assertTrue("Pitch should be negative: ${obs.headPose.pitchDegrees}", obs.headPose.pitchDegrees <= -15.0f)
        assertEquals(PostureState.SLOUCHING_DOWN, obs.postureState)
        assertTrue(obs.isSlouching)
    }

    @Test
    fun `large interocular distance classifies as too close and leaning close`() {
        // Face is very close to camera (eyeSpan > 0.35)
        val closeLandmarks = FacialLandmarks(
            leftEye = Pair(0.25f, 0.35f),
            rightEye = Pair(0.75f, 0.35f), // eyeSpan = 0.50
            noseTip = Pair(0.50f, 0.50f),
            mouthCenter = Pair(0.50f, 0.65f)
        )

        val obs = HeadPoseTransform.calculatePosture(closeLandmarks)
        assertEquals(ProximityZone.TOO_CLOSE, obs.proximity)
        assertEquals(PostureState.LEANING_CLOSE, obs.postureState)
        assertTrue(obs.isSlouching)
    }

    @Test
    fun `degenerate zero span landmarks returns neutral without NaN`() {
        val zeroLandmarks = FacialLandmarks(
            leftEye = Pair(0f, 0f),
            rightEye = Pair(0f, 0f),
            noseTip = Pair(0f, 0f),
            mouthCenter = Pair(0f, 0f)
        )

        val obs = HeadPoseTransform.calculatePosture(zeroLandmarks)
        assertEquals(PostureState.BALANCED, obs.postureState)
        assertEquals(0f, obs.headPose.rollDegrees, 0.001f)
        assertEquals(0f, obs.headPose.yawDegrees, 0.001f)
        assertEquals(0f, obs.headPose.pitchDegrees, 0.001f)
        assertFalse(obs.headPose.pitchDegrees.isNaN())
    }
}
