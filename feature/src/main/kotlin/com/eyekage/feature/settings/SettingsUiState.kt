package com.eyekage.feature.settings

import com.eyekage.core.model.EyeModeSpec
import com.eyekage.core.modes.DefaultModeCatalog

data class SettingsUiState(
    val defaultModeId: String = "mode.normal",
    val isCameraEnabled: Boolean = false,
    val reducedMotionOverride: String = "SYSTEM",
    val isHapticsEnabled: Boolean = true,
    val isDeskModeEnabled: Boolean = false,
    val availableModes: List<EyeModeSpec> = DefaultModeCatalog.getSelectableModes()
)

sealed interface SettingsUiEvent {
    data class SetDefaultMode(val modeId: String) : SettingsUiEvent
    data class SetCameraEnabled(val enabled: Boolean) : SettingsUiEvent
    data class SetReducedMotion(val option: String) : SettingsUiEvent
    data class SetHapticsEnabled(val enabled: Boolean) : SettingsUiEvent
    data class SetDeskModeEnabled(val enabled: Boolean) : SettingsUiEvent
}
