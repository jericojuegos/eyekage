package com.eyekage.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandsCardTest {

    @Test
    fun `supported phrases contains all mandatory speech recognition commands`() {
        assertEquals(8, SupportedPhrases.size)
        assertTrue(SupportedPhrases.contains("EyeKage, awaken!"))
        assertTrue(SupportedPhrases.contains("Normal eyes"))
        assertTrue(SupportedPhrases.contains("Crimson tracker"))
        assertTrue(SupportedPhrases.contains("White scan"))
        assertTrue(SupportedPhrases.contains("Purple orbit"))
        assertTrue(SupportedPhrases.contains("Sleep"))
        assertTrue(SupportedPhrases.contains("Blink"))
        assertTrue(SupportedPhrases.contains("Look at me"))
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
    fun `supported phrases are trimmed and non-empty`() {
        SupportedPhrases.forEach { phrase ->
            assertTrue("Phrase should not be blank: $phrase", phrase.isNotBlank())
            assertEquals("Phrase should have no leading/trailing whitespace", phrase.trim(), phrase)
        }
    }
}
