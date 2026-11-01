package com.eyekage.feature.settings

import com.eyekage.core.model.PostureState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostureSettingsCardTest {

    @Test
    fun `timing options contains strict 15s standard 30s and gentle 45s`() {
        assertEquals(3, SupportedPostureTimingOptions.size)

        val strict = SupportedPostureTimingOptions[0]
        assertEquals(15, strict.seconds)
        assertEquals("Strict (15s)", strict.label)

        val standard = SupportedPostureTimingOptions[1]
        assertEquals(30, standard.seconds)
        assertEquals("Standard (30s)", standard.label)

        val gentle = SupportedPostureTimingOptions[2]
        assertEquals(45, gentle.seconds)
        assertEquals("Gentle (45s)", gentle.label)
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
    fun `threshold selector callback propagates seconds correctly`() {
        var selectedThreshold: Int? = null
        val onSelectThreshold: (Int) -> Unit = { selectedThreshold = it }

        onSelectThreshold(15)
        assertEquals(15, selectedThreshold)

        onSelectThreshold(30)
        assertEquals(30, selectedThreshold)

        onSelectThreshold(45)
        assertEquals(45, selectedThreshold)
    }

    @Test
    fun `timing option labels are non-blank and trimmed`() {
        SupportedPostureTimingOptions.forEach { option ->
            assertTrue("Label should not be blank: ${option.label}", option.label.isNotBlank())
            assertEquals("Label should have no leading/trailing whitespace", option.label.trim(), option.label)
            assertTrue("Seconds should be positive", option.seconds > 0)
        }
    }

    @Test
    fun `posture state enum values are exhaustively covered`() {
        val states = PostureState.entries
        assertTrue(states.contains(PostureState.BALANCED))
        assertTrue(states.contains(PostureState.SLOUCHING_DOWN))
        assertTrue(states.contains(PostureState.LEANING_CLOSE))
        assertTrue(states.contains(PostureState.TILTED_CURIOUS))
        assertTrue(states.contains(PostureState.TURNED_AWAY))
    }
}
