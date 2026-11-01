package com.eyekage.core.behavior

import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState

sealed interface PostureAlertState {
    data object None : PostureAlertState
    data class AlertActive(
        val postureState: PostureState,
        val durationSeconds: Long
    ) : PostureAlertState
}

/**
 * Pure-Kotlin temporal tracker for desk slouching and proximity duration.
 *
 * Emits [PostureAlertState.AlertActive] only when the user sustains poor posture
 * (slouching down or leaning too close) for longer than [slouchThresholdSeconds].
 * Clears immediately when posture returns to balanced or neutral.
 */
class PostureMonitor(
    var slouchThresholdSeconds: Int = 30
) {

    private var slouchStartTimeMs: Long = 0L
    private var isAlertDismissed: Boolean = false

    fun processObservation(
        observation: PostureObservation,
        currentTimeMs: Long = System.currentTimeMillis()
    ): PostureAlertState {
        if (observation.isSlouching) {
            if (slouchStartTimeMs == 0L) {
                slouchStartTimeMs = currentTimeMs
                isAlertDismissed = false
            }

            val elapsedMs = currentTimeMs - slouchStartTimeMs
            val elapsedSec = elapsedMs / 1000L

            if (elapsedSec >= slouchThresholdSeconds && !isAlertDismissed) {
                return PostureAlertState.AlertActive(
                    postureState = observation.postureState,
                    durationSeconds = elapsedSec
                )
            }
        } else {
            // User straightened up or posture corrected
            slouchStartTimeMs = 0L
            isAlertDismissed = false
        }

        return PostureAlertState.None
    }

    /**
     * User dismissed the current alert manually (e.g. tapped HUD posture badge).
     */
    fun dismissAlert() {
        isAlertDismissed = true
    }

    fun reset() {
        slouchStartTimeMs = 0L
        isAlertDismissed = false
    }
}
