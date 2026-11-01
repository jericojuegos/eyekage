package com.eyekage.feature.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.behavior.CompanionState
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.model.MicroExpression
import com.eyekage.core.model.MoodState
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.MemoryRepository
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
    val isDeskModeEnabled: Boolean = false,
    val behaviorState: CompanionState = CompanionState.Idle,
    val showDebugOverlay: Boolean = false,
    val reducedMotion: Boolean = false,
    val microExpression: MicroExpression = MicroExpression(),
    val moodState: MoodState = MoodState.ALERT,
    val bondTier: Int = 1
)

sealed interface CompanionUiEvent {
    data class SelectMode(val modeId: String) : CompanionUiEvent
    data class ToggleCamera(val enabled: Boolean) : CompanionUiEvent
    data class ToggleDeskMode(val enabled: Boolean) : CompanionUiEvent
    data object OnScreenTapped : CompanionUiEvent
    data class OnPoke(val x: Float = 0f, val y: Float = 0f) : CompanionUiEvent
    data class RecordGazeDuration(val seconds: Long) : CompanionUiEvent
    data object ToggleDebugOverlay : CompanionUiEvent
}

class CompanionViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val faceObservationSource: FaceObservationSource,
    private val memoryRepository: MemoryRepository,
    val personalityEngine: PersonalityEngine = PersonalityEngine(),
    val behaviorStateMachine: CompanionBehaviorStateMachine = CompanionBehaviorStateMachine()
) : ViewModel() {

    private val _currentModeId = MutableStateFlow("mode.normal")
    private val _showDebugOverlay = MutableStateFlow(false)

    private val corePrefsFlow = combine(
        _currentModeId,
        preferencesRepository.cameraEnabled,
        preferencesRepository.deskModeEnabled
    ) { mode, cam, desk ->
        Triple(mode, cam, desk)
    }

    private val visionFlow = combine(
        faceObservationSource.isRunning,
        faceObservationSource.observations
    ) { running, obs ->
        Pair(running, obs)
    }

    val uiState: StateFlow<CompanionUiState> = combine(
        corePrefsFlow,
        visionFlow,
        preferencesRepository.reducedMotionOverride,
        memoryRepository.memory,
        _showDebugOverlay
    ) { (modeId, cameraEnabled, deskMode), (isRunning, observation), reducedMotion, memory, showDebug ->
        val isReducedMotion = reducedMotion == "ON"
        CompanionUiState(
            currentModeId = modeId,
            isCameraEnabled = cameraEnabled,
            isCameraTrackingActive = isRunning,
            faceObservation = observation,
            isDeskModeEnabled = deskMode,
            behaviorState = behaviorStateMachine.currentState,
            showDebugOverlay = showDebug,
            reducedMotion = isReducedMotion,
            microExpression = personalityEngine.computeMicroExpression(reducedMotion = isReducedMotion),
            moodState = personalityEngine.currentMood,
            bondTier = memory.bondTier
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
                // General screen tap
            }
            is CompanionUiEvent.OnPoke -> {
                behaviorStateMachine.poke(System.currentTimeMillis())
                personalityEngine.onPoke()
                viewModelScope.launch {
                    memoryRepository.recordPoke()
                }
            }
            is CompanionUiEvent.RecordGazeDuration -> {
                viewModelScope.launch {
                    memoryRepository.recordGazeDuration(event.seconds)
                }
            }
            is CompanionUiEvent.ToggleDebugOverlay -> {
                _showDebugOverlay.value = !_showDebugOverlay.value
            }
        }
    }
}
