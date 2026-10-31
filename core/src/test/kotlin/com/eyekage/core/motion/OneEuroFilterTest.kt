package com.eyekage.core.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sin

class OneEuroFilterTest {

    @Test
    fun `stationary signal with high-frequency noise is smoothed`() {
        val filter = OneEuroFilter(minCutoff = 1.0f, beta = 0.007f)
        val stationaryBase = 0.5f

        var maxJitterRaw = 0f
        var maxJitterFiltered = 0f

        // Simulate 60 Hz frame rate for 1.5 seconds (90 frames)
        val dt = 1.0 / 60.0
        var t = 0.0

        for (i in 0 until 90) {
            // High frequency noise with amplitude 0.05
            val noise = 0.05f * sin(2.0 * Math.PI * 25.0 * t).toFloat()
            val raw = stationaryBase + noise
            val filtered = filter.filter(raw, t)

            if (i > 10) { // After initial filter warm-up
                maxJitterRaw = maxOf(maxJitterRaw, abs(raw - stationaryBase))
                maxJitterFiltered = maxOf(maxJitterFiltered, abs(filtered - stationaryBase))
            }
            t += dt
        }

        // Filtered jitter should be substantially less than raw input jitter
        assertTrue("Filtered jitter ($maxJitterFiltered) should be less than raw jitter ($maxJitterRaw)",
            maxJitterFiltered < maxJitterRaw * 0.4f
        )
    }

    @Test
    fun `rapid step change converges quickly without infinite lag`() {
        val filter = OneEuroFilter(minCutoff = 1.0f, beta = 0.007f)
        var t = 0.0
        val dt = 1.0 / 60.0

        // Start at 0.0
        filter.filter(0.0f, t)
        t += dt

        // Sudden jump to 1.0
        val target = 1.0f
        var lastFiltered = 0f
        var stepsToReach90Percent = 0

        for (i in 0 until 60) {
            lastFiltered = filter.filter(target, t)
            if (lastFiltered >= 0.90f && stepsToReach90Percent == 0) {
                stepsToReach90Percent = i + 1
            }
            t += dt
        }

        // Must reach 90% within 25 frames (~400ms at 60fps) with beta = 0.007
        assertTrue("Steps to reach 90% ($stepsToReach90Percent) should be <= 25",
            stepsToReach90Percent in 1..25
        )
        assertEquals(1.0f, lastFiltered, 0.01f)
    }

    @Test
    fun `2D filter coordinates track and smooth both axes independently`() {
        val filter2D = OneEuroFilter2D()
        val (x1, y1) = filter2D.filter(0.2f, -0.4f, 0.0)
        assertEquals(0.2f, x1, 0.001f)
        assertEquals(-0.4f, y1, 0.001f)

        val (x2, y2) = filter2D.filter(0.25f, -0.35f, 0.016)
        assertTrue(x2 in 0.2f..0.25f)
        assertTrue(y2 in -0.4f..-0.35f)
    }

    @Test
    fun `reset clears historical state`() {
        val filter = OneEuroFilter()
        filter.filter(1.0f, 0.0)
        filter.filter(1.0f, 0.1)

        filter.reset()

        // After reset, first value should pass through directly
        val result = filter.filter(0.0f, 1.0)
        assertEquals(0.0f, result, 0.001f)
    }
}
