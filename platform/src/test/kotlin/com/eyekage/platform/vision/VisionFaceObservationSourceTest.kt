package com.eyekage.platform.vision

import com.eyekage.core.model.FaceObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class VisionFaceObservationSourceTest {

    @Test
    fun `initial state has empty observation and is not running`() {
        // Test defaults
        assertEquals(0f, FaceObservation.Empty.normalizedX, 0.001f)
        assertEquals(0f, FaceObservation.Empty.normalizedY, 0.001f)
        assertEquals(0f, FaceObservation.Empty.confidence, 0.001f)
        assertEquals(0L, FaceObservation.Empty.timestampNanos)
    }

    @Test
    fun `face observation maintains value integrity`() {
        val obs = FaceObservation(
            normalizedX = -0.75f,
            normalizedY = 0.25f,
            confidence = 0.92f,
            boundingBoxWidth = 0.35f,
            boundingBoxHeight = 0.45f
        )
        assertEquals(-0.75f, obs.normalizedX, 0.001f)
        assertEquals(0.25f, obs.normalizedY, 0.001f)
        assertEquals(0.92f, obs.confidence, 0.001f)
        assertEquals(0.35f, obs.boundingBoxWidth, 0.001f)
        assertEquals(0.45f, obs.boundingBoxHeight, 0.001f)
    }
}
