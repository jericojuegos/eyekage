package com.eyekage.ui.eyes

import androidx.compose.ui.graphics.Color
import com.eyekage.core.motion.EyeMotionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EyeSpecTest {

    @Test
    fun `default eye spec uses cyan color palette from design system`() {
        val spec = EyeSpec.Default
        assertEquals(Color(0xFF22D3EE), spec.primaryColor)
        assertEquals(Color(0xFF0891B2), spec.secondaryColor)
        assertEquals(Color(0xFFA5F3FC), spec.coreColor)
        assertTrue(spec.glowRadiusMultiplier > 1.0f)
    }

    @Test
    fun `predefined mode eye specs are valid and non-null`() {
        assertNotNull(EyeSpec.CrimsonTracker)
        assertEquals(Color(0xFFDC2626), EyeSpec.CrimsonTracker.primaryColor)

        assertNotNull(EyeSpec.WhiteScan)
        assertEquals(Color(0xFFE0E7FF), EyeSpec.WhiteScan.primaryColor)

        assertNotNull(EyeSpec.PurpleOrbit)
        assertEquals(Color(0xFFA855F7), EyeSpec.PurpleOrbit.primaryColor)
    }

    @Test
    fun `eye motion state openness and gaze boundaries`() {
        val defaultState = EyeMotionState()
        assertEquals(1.0f, defaultState.eyelidOpenness, 0.001f)
        assertEquals(0.0f, defaultState.gazeX, 0.001f)
        assertEquals(0.0f, defaultState.gazeY, 0.001f)
        assertEquals(1.0f, defaultState.pupilScale, 0.001f)

        val blinkingState = EyeMotionState(eyelidOpenness = 0.0f)
        assertEquals(0.0f, blinkingState.eyelidOpenness, 0.001f)
    }
}
