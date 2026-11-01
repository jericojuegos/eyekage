package com.eyekage.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.modes.DefaultModeCatalog
import com.eyekage.core.ports.MemoryRepository
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L
private const val PERCENT_MULTIPLIER = 100

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val memoryRepository: MemoryRepository,
    private val personalityEngine: PersonalityEngine = PersonalityEngine()
) : ViewModel() {

    private val availableModes = DefaultModeCatalog.getSelectableModes()

    private val preferencesFlow = combine(
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
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesFlow,
        memoryRepository.memory
    ) { baseState, memory ->
        val vector = personalityEngine.vector
        baseState.copy(
            memory = memory,
            currentMood = personalityEngine.currentMood,
            curiosityPercent = (vector.curiosity * PERCENT_MULTIPLIER).toInt(),
            playfulnessPercent = (vector.playfulness * PERCENT_MULTIPLIER).toInt()
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
                is SettingsUiEvent.ResetMemory -> {
                    personalityEngine.reset()
                    memoryRepository.clearMemory()
                }
            }
        }
    }
}
