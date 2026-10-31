package com.eyekage.core.modes

import com.eyekage.core.model.EyeModeId
import com.eyekage.core.model.EyeModeSpec
import com.eyekage.core.model.PupilShape

/**
 * Registry interface for retrieving supported robot eye modes and reaction overlays.
 */
interface ModeCatalog {
    fun getAllModes(): List<EyeModeSpec>
    fun getSelectableModes(): List<EyeModeSpec>
    fun getMode(id: EyeModeId): EyeModeSpec?
    fun getMode(id: String): EyeModeSpec?
    fun getModeOrDefault(id: EyeModeId): EyeModeSpec
    fun getModeOrDefault(id: String): EyeModeSpec
}

/**
 * Default implementation of [ModeCatalog] registering the 4 core selectable modes
 * and 1 transient reaction overlay using strictly neutral identifiers.
 */
object DefaultModeCatalog : ModeCatalog {

    val Normal = EyeModeSpec(
        id = EyeModeId.NORMAL,
        labelKey = "mode_label_normal",
        primaryColorArgb = 0xFF22D3EEL,
        secondaryColorArgb = 0xFF0891B2L,
        coreColorArgb = 0xFFA5F3FCL,
        glowColorArgb = 0x6622D3EEL,
        shape = PupilShape.ROUNDED_PILL,
        glowRadiusMultiplier = 1.35f,
        isSelectable = true
    )

    val CrimsonTracker = EyeModeSpec(
        id = EyeModeId.CRIMSON_TRACKER,
        labelKey = "mode_label_crimson_tracker",
        primaryColorArgb = 0xFFDC2626L,
        secondaryColorArgb = 0xFF18181BL,
        coreColorArgb = 0xFFFCA5A5L,
        glowColorArgb = 0x66DC2626L,
        shape = PupilShape.GEOMETRIC_TRACKER,
        glowRadiusMultiplier = 1.30f,
        isSelectable = true
    )

    val WhiteScan = EyeModeSpec(
        id = EyeModeId.WHITE_SCAN,
        labelKey = "mode_label_white_scan",
        primaryColorArgb = 0xFFE0E7FFL,
        secondaryColorArgb = 0xFF818CF8L,
        coreColorArgb = 0xFFFFFFFFL,
        glowColorArgb = 0x55818CF8L,
        shape = PupilShape.SCAN_LINE,
        glowRadiusMultiplier = 1.40f,
        isSelectable = true
    )

    val PurpleOrbit = EyeModeSpec(
        id = EyeModeId.PURPLE_ORBIT,
        labelKey = "mode_label_purple_orbit",
        primaryColorArgb = 0xFFA855F7L,
        secondaryColorArgb = 0xFF581C87L,
        coreColorArgb = 0xFFF3E8FFL,
        glowColorArgb = 0x66A855F7L,
        shape = PupilShape.CONCENTRIC_ORBIT,
        glowRadiusMultiplier = 1.35f,
        isSelectable = true
    )

    val Awakening = EyeModeSpec(
        id = EyeModeId.AWAKENING,
        labelKey = "reaction_label_awakening",
        primaryColorArgb = 0xFFF59E0BL,
        secondaryColorArgb = 0xFFFEF3C7L,
        coreColorArgb = 0xFFFFFFFFL,
        glowColorArgb = 0x88F59E0BL,
        shape = PupilShape.BURST_RADIAL,
        glowRadiusMultiplier = 1.50f,
        isSelectable = false
    )

    private val allModesList: List<EyeModeSpec> = listOf(
        Normal,
        CrimsonTracker,
        WhiteScan,
        PurpleOrbit,
        Awakening
    )

    private val selectableModesList: List<EyeModeSpec> = allModesList.filter { it.isSelectable }

    private val modesByIdMap: Map<String, EyeModeSpec> = allModesList.associateBy { it.id.value }

    override fun getAllModes(): List<EyeModeSpec> = allModesList

    override fun getSelectableModes(): List<EyeModeSpec> = selectableModesList

    override fun getMode(id: EyeModeId): EyeModeSpec? = modesByIdMap[id.value]

    override fun getMode(id: String): EyeModeSpec? = modesByIdMap[id]

    override fun getModeOrDefault(id: EyeModeId): EyeModeSpec = getMode(id) ?: Normal

    override fun getModeOrDefault(id: String): EyeModeSpec = getMode(id) ?: Normal
}
