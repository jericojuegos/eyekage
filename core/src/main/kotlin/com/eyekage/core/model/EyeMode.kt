package com.eyekage.core.model

/**
 * Neutral identifier for selectable robot eye modes and transient reaction overlays.
 *
 * To ensure intellectual property protection and rebrand safety, all identifiers
 * use neutral naming conventions. No anime trademark names may be used in code.
 */
@JvmInline
value class EyeModeId(val value: String) {
    companion object {
        val NORMAL = EyeModeId("mode.normal")
        val CRIMSON_TRACKER = EyeModeId("mode.crimson_tracker")
        val WHITE_SCAN = EyeModeId("mode.white_scan")
        val PURPLE_ORBIT = EyeModeId("mode.purple_orbit")
        val AWAKENING = EyeModeId("reaction.awakening")
    }
}

/**
 * Procedural geometry classification for rendering pupil and iris accents.
 */
enum class PupilShape {
    ROUNDED_PILL,
    GEOMETRIC_TRACKER,
    SCAN_LINE,
    CONCENTRIC_ORBIT,
    BURST_RADIAL
}

/**
 * Pure Kotlin data-driven specification for an eye mode or reaction overlay.
 * Uses 64-bit ARGB Longs (e.g., 0xFF22D3EEL) to remain 100% portable with zero Android dependencies.
 *
 * @param id Neutral mode identifier.
 * @param labelKey Resource string key used by UI to resolve localized display labels.
 * @param primaryColorArgb Primary eye color (iris / outer shape) in 0xAARRGGBB format.
 * @param secondaryColorArgb Secondary accent or shadow tone in 0xAARRGGBB format.
 * @param coreColorArgb Bright luminous center highlight in 0xAARRGGBB format.
 * @param glowColorArgb Outer diffuse glow color in 0xAARRGGBB format.
 * @param shape Geometric shape family for procedural iris rendering.
 * @param glowRadiusMultiplier Scale factor for diffuse outer radial glow.
 * @param isSelectable True for user-selectable modes; false for transient reaction overlays.
 */
data class EyeModeSpec(
    val id: EyeModeId,
    val labelKey: String,
    val primaryColorArgb: Long,
    val secondaryColorArgb: Long,
    val coreColorArgb: Long,
    val glowColorArgb: Long,
    val shape: PupilShape,
    val glowRadiusMultiplier: Float = 1.35f,
    val isSelectable: Boolean = true
)
