package com.eyekage.core.model

import com.eyekage.core.ports.FakeFaceObservationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceObservationTest {

    @Test
    fun `valid face observation coordinates and confidence`() {
        val observation = FaceObservation(
            normalizedX = 0.5f,
            normalizedY = -0.2f,
            confidence = 0.95f,
            boundingBoxWidth = 0.4f,
            boundingBoxHeight = 0.5f
        )

        assertEquals(0.5f, observation.normalizedX, 0.001f)
        assertEquals(-0.2f, observation.normalizedY, 0.001f)
        assertEquals(0.95f, observation.confidence, 0.001f)
        assertEquals(0.4f, observation.boundingBoxWidth, 0.001f)
        assertEquals(0.5f, observation.boundingBoxHeight, 0.001f)
        assertTrue(observation.timestampNanos > 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid confidence above 1_0 throws exception`() {
        FaceObservation(
            normalizedX = 0f,
            normalizedY = 0f,
            confidence = 1.2f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid confidence below 0_0 throws exception`() {
        FaceObservation(
            normalizedX = 0f,
            normalizedY = 0f,
            confidence = -0.1f
        )
    }

    @Test
    fun `mapCoordinates centers origin at 0_0`() {
        // Raw center (0.5, 0.5) should map to (0.0, 0.0)
        val (x, y) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.5f,
            rawCenterY = 0.5f,
            rotationDegrees = 0,
            isFrontCamera = true
        )
        assertEquals(0.0f, x, 0.001f)
        assertEquals(0.0f, y, 0.001f)
    }

    @Test
    fun `front camera mirroring moves coordinate left when user moves to physical left`() {
        // User moves to their physical left -> in front camera sensor, face is on right side (rawCenterX = 0.8)
        val (x, _) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.8f,
            rawCenterY = 0.5f,
            rotationDegrees = 0,
            isFrontCamera = true
        )
        // With mirroring, mirroredX = 1 - 0.8 = 0.2, (0.2 - 0.5) * 2 = -0.6
        assertEquals(-0.6f, x, 0.001f)
        assertTrue("X should be negative (left)", x < 0f)
    }

    @Test
    fun `front camera mirroring moves coordinate right when user moves to physical right`() {
        // User moves to their physical right -> in front camera sensor, face is on left side (rawCenterX = 0.2)
        val (x, _) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.2f,
            rawCenterY = 0.5f,
            rotationDegrees = 0,
            isFrontCamera = true
        )
        // With mirroring, mirroredX = 1 - 0.2 = 0.8, (0.8 - 0.5) * 2 = +0.6
        assertEquals(0.6f, x, 0.001f)
        assertTrue("X should be positive (right)", x > 0f)
    }

    @Test
    fun `vertical coordinates map top to negative and bottom to positive`() {
        val (_, topY) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.5f,
            rawCenterY = 0.2f,
            rotationDegrees = 0
        )
        assertEquals(-0.6f, topY, 0.001f)

        val (_, bottomY) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.5f,
            rawCenterY = 0.8f,
            rotationDegrees = 0
        )
        assertEquals(0.6f, bottomY, 0.001f)
    }

    @Test
    fun `rotation transforms apply correctly for 90 and 270 degrees`() {
        // 90 degrees rotation
        val (x90, y90) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.5f,
            rawCenterY = 0.5f,
            rotationDegrees = 90,
            isFrontCamera = true
        )
        assertEquals(0.0f, x90, 0.001f)
        assertEquals(0.0f, y90, 0.001f)

        // 270 degrees rotation (standard Android front camera sensor orientation)
        val (x270, y270) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 0.5f,
            rawCenterY = 0.5f,
            rotationDegrees = 270,
            isFrontCamera = true
        )
        assertEquals(0.0f, x270, 0.001f)
        assertEquals(0.0f, y270, 0.001f)
    }

    @Test
    fun `coordinates clamp strictly within -1_0 to 1_0`() {
        val (xClamp, yClamp) = FaceObservationTransform.mapCoordinates(
            rawCenterX = 1.5f,
            rawCenterY = -0.5f,
            rotationDegrees = 0,
            isFrontCamera = true
        )
        assertEquals(-1.0f, xClamp, 0.001f)
        assertEquals(-1.0f, yClamp, 0.001f)
    }

    @Test
    fun `fake face observation source controls flow and lifecycle`() {
        val fakeSource = FakeFaceObservationSource()
        assertFalse(fakeSource.isRunning.value)
        assertEquals(FaceObservation.Empty, fakeSource.observations.value)

        fakeSource.start()
        assertTrue(fakeSource.isRunning.value)

        fakeSource.emitCoordinates(0.3f, -0.4f, 0.85f)
        assertEquals(0.3f, fakeSource.observations.value.normalizedX, 0.001f)
        assertEquals(-0.4f, fakeSource.observations.value.normalizedY, 0.001f)
        assertEquals(0.85f, fakeSource.observations.value.confidence, 0.001f)

        fakeSource.stop()
        assertFalse(fakeSource.isRunning.value)
    }
}
