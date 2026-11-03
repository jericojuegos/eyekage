package com.eyekage.feature.settings

import com.eyekage.core.model.GestureSensitivity
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HandGestureSettingsCardTest {

    @Test
    fun `sensitivity options contains low strict, normal balanced, and high quick`() {
        assertEquals(3, SupportedGestureSensitivityOptions.size)

        val low = SupportedGestureSensitivityOptions[0]
        assertEquals(GestureSensitivity.LOW, low.sensitivity)
        assertEquals("Low (Strict)", low.label)

        val normal = SupportedGestureSensitivityOptions[1]
        assertEquals(GestureSensitivity.NORMAL, normal.sensitivity)
        assertEquals("Normal (Balanced)", normal.label)

        val high = SupportedGestureSensitivityOptions[2]
        assertEquals(GestureSensitivity.HIGH, high.sensitivity)
        assertEquals("High (Quick)", high.label)
    }

    @Test
    fun `toggle callback propagates boolean state correctly`() {
        var toggledState: Boolean? = null
        val onToggle: (Boolean) -> Unit = { toggledState = it }

        onToggle(true)
        assertEquals(true, toggledState)

        onToggle(false)
        assertEquals(false, toggledState)
    }

    @Test
    fun `sensitivity selector callback propagates sensitivity string correctly`() {
        var selectedSensitivity: String? = null
        val onSelectSensitivity: (String) -> Unit = { selectedSensitivity = it }

        onSelectSensitivity("LOW")
        assertEquals("LOW", selectedSensitivity)

        onSelectSensitivity("NORMAL")
        assertEquals("NORMAL", selectedSensitivity)

        onSelectSensitivity("HIGH")
        assertEquals("HIGH", selectedSensitivity)
    }

    @Test
    fun `sensitivity option labels are non-blank and trimmed`() {
        SupportedGestureSensitivityOptions.forEach { option ->
            assertTrue("Label should not be blank: ${option.label}", option.label.isNotBlank())
            assertEquals("Label should have no leading/trailing whitespace", option.label.trim(), option.label)
        }
    }

    @Test
    fun `hand gesture enum values are exhaustively covered`() {
        val gestures = HandGesture.entries
        assertTrue(gestures.contains(HandGesture.NONE))
        assertTrue(gestures.contains(HandGesture.WAVE))
        assertTrue(gestures.contains(HandGesture.PEACE_SIGN))
        assertTrue(gestures.contains(HandGesture.THUMBS_UP))
        assertTrue(gestures.contains(HandGesture.CLOSED_FIST))
        assertTrue(gestures.contains(HandGesture.FLAT_PALM_STOP))
    }

    @Test
    fun `empty hand observation properties match expected defaults`() {
        val empty = HandObservation.Empty
        assertEquals(HandGesture.NONE, empty.gesture)
        assertEquals(0f, empty.confidence, 0.001f)
        assertFalse(empty.isGestureDetected)
        assertEquals(0L, empty.timestampNanos)
    }
}
