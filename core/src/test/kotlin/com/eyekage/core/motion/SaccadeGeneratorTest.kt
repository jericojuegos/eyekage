package com.eyekage.core.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class SaccadeGeneratorTest {

    @Test
    fun `initial saccade offset is zero`() {
        val generator = SaccadeGenerator(random = Random(42))
        val (x, y) = generator.update(0L)
        assertEquals(0f, x, 0.0001f)
        assertEquals(0f, y, 0.0001f)
    }

    @Test
    fun `saccades trigger within 1_5 to 4_0 seconds`() {
        val generator = SaccadeGenerator(random = Random(999))
        var saccadeTriggered = false
        var saccadeTimeMs = 0L

        for (ms in 0L..6000L step 16L) {
            val (x, y) = generator.update(ms)
            if (abs(x) > 0.001f || abs(y) > 0.001f) {
                saccadeTriggered = true
                saccadeTimeMs = ms
                break
            }
        }

        assertTrue("Saccade should trigger", saccadeTriggered)
        assertTrue("Saccade triggered at $saccadeTimeMs ms, must be between 1500ms and 4000ms",
            saccadeTimeMs in 1500L..4000L
        )
    }

    @Test
    fun `saccade amplitudes stay within max amplitude bound`() {
        val maxAmp = 0.04f
        val generator = SaccadeGenerator(random = Random(123), maxAmplitude = maxAmp)

        for (ms in 0L..20000L step 100L) {
            val (x, y) = generator.update(ms)
            assertTrue("X offset $x must be within [-$maxAmp, $maxAmp]", abs(x) <= maxAmp + 0.0001f)
            assertTrue("Y offset $y must be within [-$maxAmp, $maxAmp]", abs(y) <= maxAmp + 0.0001f)
        }
    }

    @Test
    fun `reduced motion suppresses micro-saccades completely`() {
        val generator = SaccadeGenerator(random = Random(123))
        generator.reducedMotion = true

        for (ms in 0L..10000L step 50L) {
            val (x, y) = generator.update(ms)
            assertEquals(0f, x, 0.0f)
            assertEquals(0f, y, 0.0f)
        }
    }
}
