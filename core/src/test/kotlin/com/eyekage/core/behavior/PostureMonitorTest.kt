package com.eyekage.core.behavior

import com.eyekage.core.model.HeadPose
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import com.eyekage.core.model.ProximityZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostureMonitorTest {

    private lateinit var monitor: PostureMonitor

    @Before
    fun setUp() {
        monitor = PostureMonitor(slouchThresholdSeconds = 30)
    }

    private fun createObservation(state: PostureState): PostureObservation {
        return PostureObservation(
            headPose = HeadPose.Neutral,
            proximity = if (state == PostureState.LEANING_CLOSE) ProximityZone.TOO_CLOSE else ProximityZone.NORMAL,
            estimatedDistanceRatio = 0.20f,
            postureState = state
        )
    }

    @Test
    fun `balanced posture emits None`() {
        val result = monitor.processObservation(createObservation(PostureState.BALANCED))
        assertEquals(PostureAlertState.None, result)
    }

    @Test
    fun `slouching emits None during initial debounce window`() {
        var time = 1000L

        // Slouching starts at t=1000
        val r1 = monitor.processObservation(createObservation(PostureState.SLOUCHING_DOWN), currentTimeMs = time)
        assertEquals(PostureAlertState.None, r1)

        // 15 seconds later (threshold is 30s)
        time += 15_000L
        val r2 = monitor.processObservation(createObservation(PostureState.SLOUCHING_DOWN), currentTimeMs = time)
        assertEquals(PostureAlertState.None, r2)
    }

    @Test
    fun `sustained slouching beyond threshold emits AlertActive and clears when corrected`() {
        var time = 1000L

        // Slouching starts
        monitor.processObservation(createObservation(PostureState.SLOUCHING_DOWN), currentTimeMs = time)

        // 31 seconds later -> exceeds 30s threshold
        time += 31_000L
        val alert = monitor.processObservation(createObservation(PostureState.SLOUCHING_DOWN), currentTimeMs = time)
        assertTrue("Should emit AlertActive", alert is PostureAlertState.AlertActive)
        val active = alert as PostureAlertState.AlertActive
        assertEquals(PostureState.SLOUCHING_DOWN, active.postureState)
        assertEquals(31L, active.durationSeconds)

        // User straightens up (balanced)
        time += 2000L
        val corrected = monitor.processObservation(createObservation(PostureState.BALANCED), currentTimeMs = time)
        assertEquals(PostureAlertState.None, corrected)
    }

    @Test
    fun `dismissAlert suppresses alert until posture is corrected and slouched again`() {
        var time = 1000L

        // Slouch starts
        monitor.processObservation(createObservation(PostureState.LEANING_CLOSE), currentTimeMs = time)

        // Threshold reached
        time += 35_000L
        val alert = monitor.processObservation(createObservation(PostureState.LEANING_CLOSE), currentTimeMs = time)
        assertTrue(alert is PostureAlertState.AlertActive)

        // User dismisses alert
        monitor.dismissAlert()
        time += 1000L
        val dismissedResult = monitor.processObservation(createObservation(PostureState.LEANING_CLOSE), currentTimeMs = time)
        assertEquals(PostureAlertState.None, dismissedResult)

        // Posture corrected
        time += 2000L
        monitor.processObservation(createObservation(PostureState.BALANCED), currentTimeMs = time)

        // User slouches again and reaches threshold
        time += 1000L
        monitor.processObservation(createObservation(PostureState.LEANING_CLOSE), currentTimeMs = time)
        time += 31_000L
        val newAlert = monitor.processObservation(createObservation(PostureState.LEANING_CLOSE), currentTimeMs = time)
        assertTrue("Should emit alert again for new slouch session", newAlert is PostureAlertState.AlertActive)
    }
}
