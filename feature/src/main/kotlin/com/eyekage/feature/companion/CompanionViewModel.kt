package com.eyekage.feature.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

data class CompanionUiState(
    val currentModeId: String = "mode.normal",
    val isCameraEnabled: Boolean = false,
    val isCameraTrackingActive: Boolean = false,
    val faceObservation: FaceObservation = FaceObservation.Empty,
    val isDeskModeEnabled: Boolean = false
)

sealed interface CompanionUiEvent {
    data class SelectMode(val modeId: String) : CompanionUiEvent
    data class ToggleCamera(val enabled: Boolean) : CompanionUiEvent
    data class ToggleDeskMode(val enabled: Boolean) : CompanionUiEvent
    object OnScreenTapped : CompanionUiEvent
}

class CompanionViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val faceObservationSource: FaceObservationSource
) : ViewModel() {

    private val _currentModeId = MutableStateFlow("mode.normal")

    val uiState: StateFlow<CompanionUiState> = combine(
        _currentModeId,
        preferencesRepository.cameraEnabled,
        preferencesRepository.deskModeEnabled,
        faceObservationSource.isRunning,
        faceObservationSource.observations
    ) { modeId, cameraEnabled, deskMode, isRunning, observation ->
        CompanionUiState(
            currentModeId = modeId,
            isCameraEnabled = cameraEnabled,
            isCameraTrackingActive = isRunning,
            faceObservation = observation,
            isDeskModeEnabled = deskMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = CompanionUiState()
    )

    init {
        viewModelScope.launch {
            preferencesRepository.defaultModeId.collect { mode ->
                _currentModeId.value = mode
            }
        }
    }

    fun onEvent(event: CompanionUiEvent) {
        when (event) {
            is CompanionUiEvent.SelectMode -> {
                _currentModeId.value = event.modeId
                viewModelScope.launch {
                    preferencesRepository.setDefaultModeId(event.modeId)
                }
            }
            is CompanionUiEvent.ToggleCamera -> {
                viewModelScope.launch {
                    preferencesRepository.setCameraEnabled(event.enabled)
                    if (event.enabled) {
                        faceObservationSource.start()
                    } else {
                        faceObservationSource.stop()
                    }
                }
            }
            is CompanionUiEvent.ToggleDeskMode -> {
                viewModelScope.launch {
                    preferencesRepository.setDeskModeEnabled(event.enabled)
                }
            }
            is CompanionUiEvent.OnScreenTapped -> {
                // Subtle interaction handler
            }
        }
    }
}
