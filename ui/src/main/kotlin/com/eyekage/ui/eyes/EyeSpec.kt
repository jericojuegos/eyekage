package com.eyekage.ui.eyes

import androidx.compose.ui.graphics.Color

/**
 * Visual styling and geometry specification for procedural robot eyes.
 *
 * @param primaryColor Main eye color (iris / outer pill).
 * @param secondaryColor Deeper accent / shadow tone.
 * @param coreColor Bright luminous core highlight.
 * @param glowColor Diffuse ambient glow color.
 * @param glowRadiusMultiplier Scale factor for the outer radial glow.
 */
data class EyeSpec(
    val primaryColor: Color = Color(0xFF22D3EE),      // Cyan
    val secondaryColor: Color = Color(0xFF0891B2),    // Deep Cyan
    val coreColor: Color = Color(0xFFA5F3FC),         // Bright Glow Core
    val glowColor: Color = Color(0x6622D3EE),         // Soft Cyan Glow
    val glowRadiusMultiplier: Float = 1.35f
) {
    companion object {
        val Default = EyeSpec()

        // Crimson Tracker Mode palette (from design-system.md)
        val CrimsonTracker = EyeSpec(
            primaryColor = Color(0xFFDC2626),
            secondaryColor = Color(0xFF18181B),
            coreColor = Color(0xFFFCA5A5),
            glowColor = Color(0x66DC2626)
        )

        // White Scan Mode palette
        val WhiteScan = EyeSpec(
            primaryColor = Color(0xFFE0E7FF),
            secondaryColor = Color(0xFF818CF8),
            coreColor = Color(0xFFFFFFFF),
            glowColor = Color(0x55818CF8)
        )

        // Purple Orbit Mode palette
        val PurpleOrbit = EyeSpec(
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF581C87),
            coreColor = Color(0xFFF3E8FF),
            glowColor = Color(0x66A855F7)
        )
    }
}
