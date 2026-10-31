package com.eyekage.core.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BlinkSchedulerTest {

    @Test
    fun `initial state is fully open`() {
        val scheduler = BlinkScheduler(random = Random(42))
        val openness = scheduler.update(0L)
        assertEquals(1.0f, openness, 0.001f)
    }

    @Test
    fun `spontaneous blink triggers within 3 to 7 seconds`() {
        val scheduler = BlinkScheduler(random = Random(12345))
        var blinkTriggered = false
        var blinkTimeMs = 0L

        // Step through 10 seconds at 16ms intervals
        for (ms in 0L..10000L step 16L) {
            val openness = scheduler.update(ms)
            if (openness < 1.0f) {
                blinkTriggered = true
                blinkTimeMs = ms
                break
            }
        }

        assertTrue("Blink should trigger", blinkTriggered)
        assertTrue("Blink triggered at $blinkTimeMs ms, must be between 3000ms and 7000ms",
            blinkTimeMs in 3000L..7000L
        )
    }

    @Test
    fun `blink curve transitions from 1_0 to 0_0 and back to 1_0`() {
        val scheduler = BlinkScheduler(random = Random(42), blinkDurationMs = 200L)
        scheduler.triggerBlink(currentTimeMs = 1000L)

        // At start of blink (1000ms), eye is closing
        val start = scheduler.update(1000L)
        assertEquals(1.0f, start, 0.05f)

        // Mid-closing at 1040ms (~0.2 progress)
        val closing = scheduler.update(1040L)
        assertTrue("Closing openness ($closing) should be between 0.0 and 1.0", closing in 0.0f..1.0f)

        // Peak closure (~0.4 progress -> 1080ms)
        val peak = scheduler.update(1080L)
        assertTrue("Peak closure ($peak) should be close to 0.0", peak < 0.05f)

        // Reopening at 1140ms (~0.7 progress)
        val reopening = scheduler.update(1140L)
        assertTrue("Reopening openness ($reopening) should be between 0.0 and 1.0", reopening in 0.0f..1.0f)

        // Completed at 1200ms
        val completed = scheduler.update(1200L)
        assertEquals(1.0f, completed, 0.001f)
    }

    @Test
    fun `reduced motion disables progressive eyelid sweeps`() {
        val scheduler = BlinkScheduler(random = Random(42), blinkDurationMs = 200L)
        scheduler.reducedMotion = true
        scheduler.triggerBlink(currentTimeMs = 1000L)

        // In reduced motion, progress during mid-blink snaps to soft low openness without gradual cosine curve
        val mid = scheduler.update(1100L)
        assertTrue(mid == 0.1f || mid == 1.0f)

        val end = scheduler.update(1200L)
        assertEquals(1.0f, end, 0.001f)
    }
}
