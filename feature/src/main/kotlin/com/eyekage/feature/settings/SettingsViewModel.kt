package com.eyekage.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.model.HandObservation
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.modes.DefaultModeCatalog
import com.eyekage.core.ports.HandGestureSource
import com.eyekage.core.ports.MemoryRepository
import com.eyekage.core.ports.PostureObservationSource
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L
private const val PERCENT_MULTIPLIER = 100

private data class PostureAndVoiceState(
    val voiceEnabled: Boolean,
    val postureReminderEnabled: Boolean,
    val postureSlouchThresholdSec: Int,
    val postureObservation: PostureObservation
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val memoryRepository: MemoryRepository,
    private val personalityEngine: PersonalityEngine = PersonalityEngine(),
    private val postureObservationSource: PostureObservationSource? = null,
    private val handGestureSource: HandGestureSource? = null
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

    private val postureAndVoiceFlow = combine(
        preferencesRepository.voiceCommandsEnabled,
        preferencesRepository.postureReminderEnabled,
        preferencesRepository.postureSlouchThresholdSec,
        postureObservationSource?.posture ?: flowOf(PostureObservation.Neutral)
    ) { voice, postureReminder, thresholdSec, postureObs ->
        PostureAndVoiceState(
            voiceEnabled = voice,
            postureReminderEnabled = postureReminder,
            postureSlouchThresholdSec = thresholdSec,
            postureObservation = postureObs
        )
    }

    private val gestureFlow = combine(
        preferencesRepository.gestureRecognitionEnabled,
        preferencesRepository.gestureSensitivity,
        handGestureSource?.handGestures ?: flowOf(HandObservation.Empty)
    ) { enabled, sensitivity, gestureObs ->
        Triple(enabled, sensitivity, gestureObs)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesFlow,
        postureAndVoiceFlow,
        gestureFlow,
        memoryRepository.memory
    ) { baseState, postureAndVoice, gestureData, memory ->
        val vector = personalityEngine.vector
        baseState.copy(
            isVoiceCommandsEnabled = postureAndVoice.voiceEnabled,
            isPostureReminderEnabled = postureAndVoice.postureReminderEnabled,
            postureSlouchThresholdSec = postureAndVoice.postureSlouchThresholdSec,
            currentPostureState = postureAndVoice.postureObservation.postureState,
            isGestureRecognitionEnabled = gestureData.first,
            gestureSensitivity = gestureData.second,
            currentHandGesture = gestureData.third,
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
                is SettingsUiEvent.SetVoiceCommandsEnabled -> {
                    preferencesRepository.setVoiceCommandsEnabled(event.enabled)
                }
                is SettingsUiEvent.SetPostureReminderEnabled -> {
                    preferencesRepository.setPostureReminderEnabled(event.enabled)
                }
                is SettingsUiEvent.SetPostureSlouchThreshold -> {
                    preferencesRepository.setPostureSlouchThresholdSec(event.seconds)
                }
                is SettingsUiEvent.SetGestureRecognitionEnabled -> {
                    preferencesRepository.setGestureRecognitionEnabled(event.enabled)
                }
                is SettingsUiEvent.SetGestureSensitivity -> {
                    preferencesRepository.setGestureSensitivity(event.sensitivity)
                }
                is SettingsUiEvent.ResetMemory -> {
                    personalityEngine.reset()
                    memoryRepository.clearMemory()
                }
            }
        }
    }
}
