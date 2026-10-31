package com.eyekage.ui.eyes

import androidx.compose.ui.graphics.Color
import com.eyekage.core.model.EyeModeId
import com.eyekage.core.model.PupilShape
import com.eyekage.core.modes.DefaultModeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EyeModeRenderingTest {

    @Test
    fun `resolving spec from all core mode IDs preserves shape and design tokens`() {
        val normal = EyeSpec.fromModeId(EyeModeId.NORMAL.value)
        assertEquals(Color(0xFF22D3EE), normal.primaryColor)
        assertEquals(PupilShape.ROUNDED_PILL, normal.shape)

        val crimson = EyeSpec.fromModeId(EyeModeId.CRIMSON_TRACKER.value)
        assertEquals(Color(0xFFDC2626), crimson.primaryColor)
        assertEquals(PupilShape.GEOMETRIC_TRACKER, crimson.shape)

        val whiteScan = EyeSpec.fromModeId(EyeModeId.WHITE_SCAN.value)
        assertEquals(Color(0xFFE0E7FF), whiteScan.primaryColor)
        assertEquals(PupilShape.SCAN_LINE, whiteScan.shape)

        val purpleOrbit = EyeSpec.fromModeId(EyeModeId.PURPLE_ORBIT.value)
        assertEquals(Color(0xFFA855F7), purpleOrbit.primaryColor)
        assertEquals(PupilShape.CONCENTRIC_ORBIT, purpleOrbit.shape)

        val awakening = EyeSpec.fromModeId(EyeModeId.AWAKENING.value)
        assertEquals(Color(0xFFF59E0B), awakening.primaryColor)
        assertEquals(PupilShape.BURST_RADIAL, awakening.shape)
    }

    @Test
    fun `unknown mode ID resolves to default normal eye spec`() {
        val fallback = EyeSpec.fromModeId("mode.non_existent")
        assertEquals(EyeSpec.Default, fallback)
        assertEquals(Color(0xFF22D3EE), fallback.primaryColor)
        assertEquals(PupilShape.ROUNDED_PILL, fallback.shape)
    }

    @Test
    fun `conversion from EyeModeSpec to EyeSpec accurately converts 64-bit ARGB values`() {
        val spec = DefaultModeCatalog.CrimsonTracker
        val composeSpec = EyeSpec.fromMode(spec)

        assertEquals(Color(0xFFDC2626), composeSpec.primaryColor)
        assertEquals(Color(0xFF18181B), composeSpec.secondaryColor)
        assertEquals(Color(0xFFFCA5A5), composeSpec.coreColor)
        assertEquals(Color(0x66DC2626), composeSpec.glowColor)
        assertEquals(PupilShape.GEOMETRIC_TRACKER, composeSpec.shape)
        assertEquals(1.30f, composeSpec.glowRadiusMultiplier, 0.001f)
    }
}
