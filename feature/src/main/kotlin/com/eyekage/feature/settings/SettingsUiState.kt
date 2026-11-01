package com.eyekage.feature.settings

import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.EyeModeSpec
import com.eyekage.core.model.MoodState
import com.eyekage.core.modes.DefaultModeCatalog

data class SettingsUiState(
    val defaultModeId: String = "mode.normal",
    val isCameraEnabled: Boolean = false,
    val reducedMotionOverride: String = "SYSTEM",
    val isHapticsEnabled: Boolean = true,
    val isDeskModeEnabled: Boolean = false,
    val isVoiceCommandsEnabled: Boolean = false,
    val availableModes: List<EyeModeSpec> = DefaultModeCatalog.getSelectableModes(),
    val memory: CompanionMemory = CompanionMemory(),
    val currentMood: MoodState = MoodState.ALERT,
    val curiosityPercent: Int = 50,
    val playfulnessPercent: Int = 50
)

sealed interface SettingsUiEvent {
    data class SetDefaultMode(val modeId: String) : SettingsUiEvent
    data class SetCameraEnabled(val enabled: Boolean) : SettingsUiEvent
    data class SetReducedMotion(val option: String) : SettingsUiEvent
    data class SetHapticsEnabled(val enabled: Boolean) : SettingsUiEvent
    data class SetDeskModeEnabled(val enabled: Boolean) : SettingsUiEvent
    data class SetVoiceCommandsEnabled(val enabled: Boolean) : SettingsUiEvent
    data object ResetMemory : SettingsUiEvent
}
