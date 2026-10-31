package com.eyekage.ui.eyes

import androidx.compose.ui.graphics.Color
import com.eyekage.core.model.EyeModeSpec
import com.eyekage.core.model.PupilShape
import com.eyekage.core.modes.DefaultModeCatalog

/**
 * Visual styling and geometry specification for procedural robot eyes in Compose.
 *
 * @param primaryColor Main eye color (iris / outer pill).
 * @param secondaryColor Deeper accent / shadow tone.
 * @param coreColor Bright luminous core highlight.
 * @param glowColor Diffuse ambient glow color.
 * @param glowRadiusMultiplier Scale factor for the outer radial glow.
 * @param shape Procedural pupil/iris geometry shape.
 */
data class EyeSpec(
    val primaryColor: Color = Color(0xFF22D3EE),      // Cyan
    val secondaryColor: Color = Color(0xFF0891B2),    // Deep Cyan
    val coreColor: Color = Color(0xFFA5F3FC),         // Bright Glow Core
    val glowColor: Color = Color(0x6622D3EE),         // Soft Cyan Glow
    val glowRadiusMultiplier: Float = 1.35f,
    val shape: PupilShape = PupilShape.ROUNDED_PILL
) {
    companion object {
        val Default: EyeSpec = fromMode(DefaultModeCatalog.Normal)
        val CrimsonTracker: EyeSpec = fromMode(DefaultModeCatalog.CrimsonTracker)
        val WhiteScan: EyeSpec = fromMode(DefaultModeCatalog.WhiteScan)
        val PurpleOrbit: EyeSpec = fromMode(DefaultModeCatalog.PurpleOrbit)
        val Awakening: EyeSpec = fromMode(DefaultModeCatalog.Awakening)

        /**
         * Converts a pure-Kotlin [EyeModeSpec] from domain core into a Compose [EyeSpec].
         */
        fun fromMode(mode: EyeModeSpec): EyeSpec = EyeSpec(
            primaryColor = Color(mode.primaryColorArgb),
            secondaryColor = Color(mode.secondaryColorArgb),
            coreColor = Color(mode.coreColorArgb),
            glowColor = Color(mode.glowColorArgb),
            glowRadiusMultiplier = mode.glowRadiusMultiplier,
            shape = mode.shape
        )

        /**
         * Resolves an [EyeSpec] for a given mode ID string, falling back to [Default] if unknown.
         */
        fun fromModeId(id: String): EyeSpec = fromMode(DefaultModeCatalog.getModeOrDefault(id))
    }
}
