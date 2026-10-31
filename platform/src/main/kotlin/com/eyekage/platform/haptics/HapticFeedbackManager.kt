package com.eyekage.platform.haptics

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Handles tactile feedback using [View.performHapticFeedback],
 * completely eliminating the need for `android.permission.VIBRATE`.
 */
class HapticFeedbackManager {

    fun performTap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun performHeavyClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}
