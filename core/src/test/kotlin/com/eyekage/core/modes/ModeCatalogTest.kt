package com.eyekage.core.modes

import com.eyekage.core.model.EyeModeId
import com.eyekage.core.model.PupilShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeCatalogTest {

    @Test
    fun `catalog contains exactly four selectable modes and one reaction overlay`() {
        val allModes = DefaultModeCatalog.getAllModes()
        assertEquals(5, allModes.size)

        val selectableModes = DefaultModeCatalog.getSelectableModes()
        assertEquals(4, selectableModes.size)

        val reactionModes = allModes.filter { !it.isSelectable }
        assertEquals(1, reactionModes.size)
        assertEquals(EyeModeId.AWAKENING, reactionModes.first().id)
    }

    @Test
    fun `selectable modes use strictly neutral identifiers`() {
        val expectedSelectableIds = setOf(
            "mode.normal",
            "mode.crimson_tracker",
            "mode.white_scan",
            "mode.purple_orbit"
        )
        val actualSelectableIds = DefaultModeCatalog.getSelectableModes().map { it.id.value }.toSet()
        assertEquals(expectedSelectableIds, actualSelectableIds)
    }

    @Test
    fun `lookup by id returns correct mode or null`() {
        val normal = DefaultModeCatalog.getMode(EyeModeId.NORMAL)
        assertNotNull(normal)
        assertEquals("mode.normal", normal?.id?.value)
        assertEquals(PupilShape.ROUNDED_PILL, normal?.shape)

        val crimson = DefaultModeCatalog.getMode("mode.crimson_tracker")
        assertNotNull(crimson)
        assertEquals(PupilShape.GEOMETRIC_TRACKER, crimson?.shape)

        val nonExistent = DefaultModeCatalog.getMode("mode.unknown_xyz")
        assertNull(nonExistent)
    }

    @Test
    fun `lookup or default returns Normal when id is unrecognized`() {
        val defaultFromUnknown = DefaultModeCatalog.getModeOrDefault("mode.invalid")
        assertEquals(DefaultModeCatalog.Normal, defaultFromUnknown)

        val defaultFromId = DefaultModeCatalog.getModeOrDefault(EyeModeId("unrecognized"))
        assertEquals(DefaultModeCatalog.Normal, defaultFromId)
    }

    @Test
    fun `all registered mode colors have valid alpha channels`() {
        DefaultModeCatalog.getAllModes().forEach { mode ->
            // Check that primary, secondary, and core have 0xFF alpha channel (upper 8 bits)
            val primaryAlpha = (mode.primaryColorArgb ushr 24) and 0xFFL
            val secondaryAlpha = (mode.secondaryColorArgb ushr 24) and 0xFFL
            val coreAlpha = (mode.coreColorArgb ushr 24) and 0xFFL
            val glowAlpha = (mode.glowColorArgb ushr 24) and 0xFFL

            assertEquals("Primary color alpha must be 0xFF for ${mode.id.value}", 0xFFL, primaryAlpha)
            assertEquals("Secondary color alpha must be 0xFF for ${mode.id.value}", 0xFFL, secondaryAlpha)
            assertEquals("Core color alpha must be 0xFF for ${mode.id.value}", 0xFFL, coreAlpha)
            assertTrue("Glow alpha must be non-zero for ${mode.id.value}", glowAlpha > 0L)
        }
    }

    @Test
    fun `rebrand safety audit verifies absence of anime trademark terms in core code`() {
        val prohibitedTerms = listOf("sharingan", "byakugan", "rinnegan", "naruto", "uchiha", "hyuga")

        DefaultModeCatalog.getAllModes().forEach { mode ->
            prohibitedTerms.forEach { term ->
                assertFalse(
                    "Mode ID '${mode.id.value}' must not contain trademark term '$term'",
                    mode.id.value.lowercase().contains(term)
                )
                assertFalse(
                    "Label key '${mode.labelKey}' must not contain trademark term '$term'",
                    mode.labelKey.lowercase().contains(term)
                )
            }
        }
    }
}
