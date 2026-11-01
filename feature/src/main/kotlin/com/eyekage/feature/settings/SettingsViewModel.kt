package com.eyekage.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyekage.core.modes.DefaultModeCatalog
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val availableModes = DefaultModeCatalog.getSelectableModes()

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.defaultModeId,
        preferencesRepository.cameraEnabled,
        preferencesRepository.reducedMotionOverride,
        preferencesRepository.hapticsEnabled,
        preferencesRepository.deskModeEnabled
    ) { modeId, camera, motion, haptics, desk ->
        SettingsUiState(
            defaultModeId = modeId,
            isCameraEnabled = camera,
            reducedMotionOverride = motion,
            isHapticsEnabled = haptics,
            isDeskModeEnabled = desk,
            availableModes = availableModes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = SettingsUiState(availableModes = availableModes)
    )

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsUiEvent.SetDefaultMode -> {
                    preferencesRepository.setDefaultModeId(event.modeId)
                }
                is SettingsUiEvent.SetCameraEnabled -> {
                    preferencesRepository.setCameraEnabled(event.enabled)
                }
                is SettingsUiEvent.SetReducedMotion -> {
                    preferencesRepository.setReducedMotionOverride(event.option)
                }
                is SettingsUiEvent.SetHapticsEnabled -> {
                    preferencesRepository.setHapticsEnabled(event.enabled)
                }
                is SettingsUiEvent.SetDeskModeEnabled -> {
                    preferencesRepository.setDeskModeEnabled(event.enabled)
                }
            }
        }
    }
}
