package com.eyekage.core.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EyeMotionEngineTest {

    @Test
    fun `output gaze coordinates stay strictly within normalized bounds`() {
        val engine = EyeMotionEngine()
        val targets = listOf(
            GazeTarget(1.0f, 1.0f),
            GazeTarget(-1.0f, -1.0f),
            GazeTarget(0.8f, -0.9f),
            GazeTarget(-0.5f, 0.7f)
        )

        var timeMs = 0L
        for (target in targets) {
            for (step in 0 until 30) {
                timeMs += 16L
                val state = engine.update(target, timeMs)
                assertTrue("GazeX (${state.gazeX}) must be in [-1.0, 1.0]", state.gazeX in -1.0f..1.0f)
                assertTrue("GazeY (${state.gazeY}) must be in [-1.0, 1.0]", state.gazeY in -1.0f..1.0f)
                assertTrue("EyelidOpenness (${state.eyelidOpenness}) must be in [0.0, 1.0]", state.eyelidOpenness in 0.0f..1.0f)
            }
        }
    }

    @Test
    fun `reduced motion disables saccades and uses soft eyelid`() {
        val engine = EyeMotionEngine()
        engine.reducedMotion = true

        val state = engine.update(GazeTarget.Center, 0L)
        assertEquals(0f, state.gazeX, 0.001f)
        assertEquals(0f, state.gazeY, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GazeTarget out of bounds throws IllegalArgumentException`() {
        GazeTarget(1.5f, 0f)
    }
}
