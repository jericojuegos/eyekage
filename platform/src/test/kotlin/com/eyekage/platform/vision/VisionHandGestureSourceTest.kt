package com.eyekage.platform.vision

import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandObservation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VisionHandGestureSourceTest {

    private lateinit var source: VisionHandGestureSource

    @Before
    fun setUp() {
        source = VisionHandGestureSource(context = null)
    }

    @Test
    fun `initial state is not tracking and has empty observation`() = runTest {
        assertFalse(source.isTracking.first())
        val obs = source.handGestures.first()
        assertEquals(HandGesture.NONE, obs.gesture)
        assertEquals(0f, obs.confidence, 0.001f)
        assertFalse(obs.isGestureDetected)
    }

    @Test
    fun `start and stop manage tracking lifecycle and reset state`() = runTest {
        source.start()
        assertTrue(source.isTracking.first())

        source.emitObservation(
            gesture = HandGesture.WAVE,
            confidence = 0.90f
        )
        val activeObs = source.handGestures.first()
        assertEquals(HandGesture.WAVE, activeObs.gesture)

        source.stop()
        assertFalse(source.isTracking.first())
        assertEquals(HandObservation.Empty, source.handGestures.first())
    }

    @Test
    fun `emitObservation correctly maps coordinates with front camera mirroring`() = runTest {
        source.start()

        // Hand on user's right side (rawCenterX = 0.8f in image space)
        // With front camera mirroring, 1.0 - 0.8 = 0.2 -> normalized to -0.6f
        source.emitObservation(
            gesture = HandGesture.PEACE_SIGN,
            confidence = 0.88f,
            rawCenterX = 0.8f,
            rawCenterY = 0.5f,
            rotationDegrees = 0
        )

        val obs = source.handGestures.first()
        assertEquals(HandGesture.PEACE_SIGN, obs.gesture)
        assertEquals(0.88f, obs.confidence, 0.001f)
        assertTrue("Front camera mirroring should invert horizontal center", obs.normalizedCenterX!! < 0f)
        assertEquals(0f, obs.normalizedCenterY!!, 0.001f)
    }

    @Test
    fun `mapCategoryToHandGesture maps standard MediaPipe categories accurately`() {
        assertEquals(HandGesture.WAVE, VisionHandGestureSource.mapCategoryToHandGesture("open_palm"))
        assertEquals(HandGesture.PEACE_SIGN, VisionHandGestureSource.mapCategoryToHandGesture("victory"))
        assertEquals(HandGesture.THUMBS_UP, VisionHandGestureSource.mapCategoryToHandGesture("thumb_up"))
        assertEquals(HandGesture.CLOSED_FIST, VisionHandGestureSource.mapCategoryToHandGesture("closed_fist"))
        assertEquals(HandGesture.FLAT_PALM_STOP, VisionHandGestureSource.mapCategoryToHandGesture("stop"))
        assertEquals(HandGesture.FLAT_PALM_STOP, VisionHandGestureSource.mapCategoryToHandGesture("flat_palm"))
        assertEquals(HandGesture.NONE, VisionHandGestureSource.mapCategoryToHandGesture("unknown"))
        assertEquals(HandGesture.NONE, VisionHandGestureSource.mapCategoryToHandGesture("None"))
    }
}
