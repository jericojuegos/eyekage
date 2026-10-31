package com.eyekage.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceObservationTest {

    @Test
    fun `valid face observation coordinates and confidence`() {
        val observation = FaceObservation(
            normalizedX = 0.5f,
            normalizedY = -0.2f,
            confidence = 0.95f
        )

        assertEquals(0.5f, observation.normalizedX, 0.001f)
        assertEquals(-0.2f, observation.normalizedY, 0.001f)
        assertEquals(0.95f, observation.confidence, 0.001f)
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
}
