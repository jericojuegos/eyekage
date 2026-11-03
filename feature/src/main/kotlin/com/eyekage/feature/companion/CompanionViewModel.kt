package com.eyekage.feature.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.behavior.CompanionState
import com.eyekage.core.behavior.HandGestureReactionEngine
import com.eyekage.core.behavior.HeadPoseGesture
import com.eyekage.core.behavior.HeadPoseReactionEngine
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.behavior.PostureAlertState
import com.eyekage.core.behavior.PostureMonitor
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.model.GestureSensitivity
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandGestureReaction
import com.eyekage.core.model.HandObservation
import com.eyekage.core.model.MicroExpression
import com.eyekage.core.model.MoodState
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.VoiceCommand
import com.eyekage.core.model.VoiceCommandEvent
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.HandGestureSource
import com.eyekage.core.ports.MemoryRepository
import com.eyekage.core.ports.PostureObservationSource
import com.eyekage.core.ports.PreferencesRepository
import com.eyekage.core.ports.VoiceCommandSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val isVoiceCommandsEnabled: Boolean = false,
    val isVoiceListening: Boolean = false,
    val behaviorState: CompanionState = CompanionState.Idle,
    val showDebugOverlay: Boolean = false,
    val reducedMotion: Boolean = false,
    val microExpression: MicroExpression = MicroExpression(),
    val moodState: MoodState = MoodState.ALERT,
    val bondTier: Int = 1,
    val postureObservation: PostureObservation = PostureObservation.Neutral,
    val eyeTiltDegrees: Float = 0.0f,
    val postureAlert: PostureAlertState = PostureAlertState.None,
    val isPostureReminderEnabled: Boolean = false,
    val activeHandGesture: HandObservation = HandObservation.Empty,
    val handGestureReaction: HandGestureReaction = HandGestureReaction.None,
    val leftEyeOpennessMultiplier: Float = 1.0f,
    val rightEyeOpennessMultiplier: Float = 1.0f,
    val isGestureRecognitionEnabled: Boolean = true,
    val gestureSensitivity: String = "NORMAL",
    val activeGestureFeedbackLabel: String? = null
)

sealed interface CompanionUiEvent {
    data class SelectMode(val modeId: String) : CompanionUiEvent
    data class ToggleCamera(val enabled: Boolean) : CompanionUiEvent
    data class ToggleDeskMode(val enabled: Boolean) : CompanionUiEvent
    data class ToggleVoiceCommands(val enabled: Boolean) : CompanionUiEvent
    data class ToggleHandGestures(val enabled: Boolean) : CompanionUiEvent
    data object OnScreenTapped : CompanionUiEvent
    data class OnPoke(val x: Float = 0f, val y: Float = 0f) : CompanionUiEvent
    data class RecordGazeDuration(val seconds: Long) : CompanionUiEvent
    data object ToggleDebugOverlay : CompanionUiEvent
    data object OnStartListeningVoice : CompanionUiEvent
    data object OnStopListeningVoice : CompanionUiEvent
    data object DismissPostureAlert : CompanionUiEvent
    data object DismissGestureBadge : CompanionUiEvent
}

class CompanionViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val faceObservationSource: FaceObservationSource,
    private val memoryRepository: MemoryRepository,
    val personalityEngine: PersonalityEngine = PersonalityEngine(),
    val behaviorStateMachine: CompanionBehaviorStateMachine = CompanionBehaviorStateMachine(),
    val voiceCommandSource: VoiceCommandSource? = null,
    val postureObservationSource: PostureObservationSource? = null,
    val headPoseReactionEngine: HeadPoseReactionEngine = HeadPoseReactionEngine(),
    val postureMonitor: PostureMonitor = PostureMonitor(),
    val handGestureSource: HandGestureSource? = null,
    val handGestureReactionEngine: HandGestureReactionEngine = HandGestureReactionEngine()
) : ViewModel() {

    private val _currentModeId = MutableStateFlow("mode.normal")
    private val _showDebugOverlay = MutableStateFlow(false)
    private val _eyeTiltDegrees = MutableStateFlow(0f)
    private val _postureAlert = MutableStateFlow<PostureAlertState>(PostureAlertState.None)
    private val _postureObservation = MutableStateFlow(PostureObservation.Neutral)

    private val _activeHandGesture = MutableStateFlow(HandObservation.Empty)
    private val _handGestureReaction = MutableStateFlow<HandGestureReaction>(HandGestureReaction.None)
    private val _leftEyeOpennessMultiplier = MutableStateFlow(1.0f)
    private val _rightEyeOpennessMultiplier = MutableStateFlow(1.0f)
    private val _gestureFeedbackLabel = MutableStateFlow<String?>(null)
    private var feedbackDismissalJob: Job? = null

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

    private val voiceFlow = combine(
        preferencesRepository.voiceCommandsEnabled,
        voiceCommandSource?.isListening ?: MutableStateFlow(false)
    ) { enabled, listening ->
        Pair(enabled, listening)
    }

    private val displayPrefsFlow = combine(
        preferencesRepository.reducedMotionOverride,
        _showDebugOverlay
    ) { motion, debug ->
        Pair(motion, debug)
    }

    private val postureFlow = combine(
        _eyeTiltDegrees,
        _postureAlert,
        _postureObservation,
        preferencesRepository.postureReminderEnabled
    ) { tilt, alert, obs, reminderEnabled ->
        PostureData(tilt, alert, obs, reminderEnabled)
    }

    private data class PostureData(
        val tilt: Float,
        val alert: PostureAlertState,
        val obs: PostureObservation,
        val reminderEnabled: Boolean
    )

    private data class GestureUiData(
        val obs: HandObservation,
        val reaction: HandGestureReaction,
        val leftOpen: Float,
        val rightOpen: Float,
        val label: String?,
        val enabled: Boolean,
        val sensitivity: String
    )

    private val gestureFlow = combine(
        _activeHandGesture,
        _handGestureReaction,
        _leftEyeOpennessMultiplier,
        _rightEyeOpennessMultiplier,
        _gestureFeedbackLabel
    ) { obs, reaction, leftOpen, rightOpen, label ->
        GestureInternal(obs, reaction, leftOpen, rightOpen, label)
    }

    private data class GestureInternal(
        val obs: HandObservation,
        val reaction: HandGestureReaction,
        val leftOpen: Float,
        val rightOpen: Float,
        val label: String?
    )

    private val combinedGestureFlow = combine(
        gestureFlow,
        preferencesRepository.gestureRecognitionEnabled,
        preferencesRepository.gestureSensitivity
    ) { internalData, enabled, sensitivity ->
        GestureUiData(
            obs = internalData.obs,
            reaction = internalData.reaction,
            leftOpen = internalData.leftOpen,
            rightOpen = internalData.rightOpen,
            label = internalData.label,
            enabled = enabled,
            sensitivity = sensitivity
        )
    }

    private val awarenessFlow = combine(
        visionFlow,
        postureFlow,
        combinedGestureFlow
    ) { vision, postureData, gestureData ->
        AwarenessData(vision, postureData, gestureData)
    }

    private data class AwarenessData(
        val vision: Pair<Boolean, FaceObservation>,
        val posture: PostureData,
        val gesture: GestureUiData
    )

    val uiState: StateFlow<CompanionUiState> = combine(
        corePrefsFlow,
        awarenessFlow,
        voiceFlow,
        displayPrefsFlow,
        memoryRepository.memory
    ) { corePrefs, awareness, voice, displayPrefs, memory ->
        val (modeId, cameraEnabled, deskMode) = corePrefs
        val (isRunning, observation) = awareness.vision
        val postureData = awareness.posture
        val gestureData = awareness.gesture
        val (voiceEnabled, isListening) = voice
        val (reducedMotion, showDebug) = displayPrefs
        val isReducedMotion = reducedMotion == "ON"
        CompanionUiState(
            currentModeId = modeId,
            isCameraEnabled = cameraEnabled,
            isCameraTrackingActive = isRunning,
            faceObservation = observation,
            isDeskModeEnabled = deskMode,
            isVoiceCommandsEnabled = voiceEnabled,
            isVoiceListening = isListening,
            behaviorState = behaviorStateMachine.currentState,
            showDebugOverlay = showDebug,
            reducedMotion = isReducedMotion,
            microExpression = personalityEngine.computeMicroExpression(reducedMotion = isReducedMotion),
            moodState = personalityEngine.currentMood,
            bondTier = memory.bondTier,
            postureObservation = postureData.obs,
            eyeTiltDegrees = postureData.tilt,
            postureAlert = postureData.alert,
            isPostureReminderEnabled = postureData.reminderEnabled,
            activeHandGesture = gestureData.obs,
            handGestureReaction = gestureData.reaction,
            leftEyeOpennessMultiplier = gestureData.leftOpen,
            rightEyeOpennessMultiplier = gestureData.rightOpen,
            isGestureRecognitionEnabled = gestureData.enabled,
            gestureSensitivity = gestureData.sensitivity,
            activeGestureFeedbackLabel = gestureData.label
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

        viewModelScope.launch {
            var wasActive = false
            preferencesRepository.voiceCommandsEnabled.collect { enabled ->
                if (enabled) {
                    wasActive = true
                    voiceCommandSource?.startListening()
                } else if (wasActive) {
                    wasActive = false
                    voiceCommandSource?.stopListening()
                }
            }
        }

        viewModelScope.launch {
            voiceCommandSource?.events?.collect { event ->
                handleVoiceEvent(event)
            }
        }

        viewModelScope.launch {
            preferencesRepository.postureSlouchThresholdSec.collect { threshold ->
                postureMonitor.slouchThresholdSeconds = threshold
            }
        }

        if (postureObservationSource != null) {
            viewModelScope.launch {
                combine(
                    postureObservationSource.posture,
                    preferencesRepository.postureReminderEnabled,
                    preferencesRepository.reducedMotionOverride
                ) { obs, reminderEnabled, reducedMotion ->
                    Triple(obs, reminderEnabled, reducedMotion == "ON")
                }.collect { (obs, reminderEnabled, isReducedMotion) ->
                    _postureObservation.value = obs
                    val now = if (obs.timestampNanos > 0L) obs.timestampNanos / 1_000_000L else System.currentTimeMillis()

                    val gesture = headPoseReactionEngine.processObservation(
                        observation = obs,
                        currentTimeMs = now,
                        reducedMotion = isReducedMotion
                    )

                    when (gesture) {
                        is HeadPoseGesture.InquisitiveTilt -> {
                            _eyeTiltDegrees.value = gesture.eyeTiltDegrees
                        }
                        is HeadPoseGesture.AffirmativeNod -> {
                            behaviorStateMachine.blink(now)
                            personalityEngine.onPoke()
                            viewModelScope.launch {
                                memoryRepository.recordPoke()
                            }
                        }
                        is HeadPoseGesture.TurnedAway, HeadPoseGesture.Idle -> {
                            _eyeTiltDegrees.value = 0f
                        }
                    }

                    if (reminderEnabled) {
                        val alert = postureMonitor.processObservation(obs, now)
                        _postureAlert.value = alert
                    } else {
                        postureMonitor.reset()
                        _postureAlert.value = PostureAlertState.None
                    }
                }
            }
        }

        if (handGestureSource != null) {
            viewModelScope.launch {
                combine(
                    handGestureSource.handGestures,
                    preferencesRepository.gestureRecognitionEnabled,
                    preferencesRepository.gestureSensitivity
                ) { obs, enabled, sensitivity ->
                    Triple(obs, enabled, sensitivity)
                }.collect { (obs, enabled, sensitivity) ->
                    handGestureReactionEngine.sensitivity = GestureSensitivity.fromString(sensitivity)
                    val now = if (obs.timestampNanos > 0L) obs.timestampNanos / 1_000_000L else System.currentTimeMillis()
                    val reaction = handGestureReactionEngine.processObservation(
                        observation = obs,
                        currentTimeMs = now,
                        isEnabled = enabled
                    )
                    _activeHandGesture.value = obs
                    _handGestureReaction.value = reaction

                    if (!enabled) {
                        _leftEyeOpennessMultiplier.value = 1.0f
                        _rightEyeOpennessMultiplier.value = 1.0f
                        _gestureFeedbackLabel.value = null
                        return@collect
                    }

                    when (reaction) {
                        is HandGestureReaction.WaveGreeting -> {
                            _gestureFeedbackLabel.value = "👋 Wave Detected!"
                            personalityEngine.onPoke()
                            behaviorStateMachine.blink(now)
                            viewModelScope.launch {
                                memoryRepository.recordPoke()
                            }
                            launchFeedbackDismissal()
                        }
                        is HandGestureReaction.PeaceWink -> {
                            _gestureFeedbackLabel.value = "✌️ Peace Sign!"
                            if (reaction.isLeftEye) {
                                _leftEyeOpennessMultiplier.value = 0.0f
                                _rightEyeOpennessMultiplier.value = 1.0f
                            } else {
                                _leftEyeOpennessMultiplier.value = 1.0f
                                _rightEyeOpennessMultiplier.value = 0.0f
                            }
                            viewModelScope.launch {
                                delay(600L)
                                _leftEyeOpennessMultiplier.value = 1.0f
                                _rightEyeOpennessMultiplier.value = 1.0f
                            }
                            launchFeedbackDismissal()
                        }
                        is HandGestureReaction.ThumbsUpApproval -> {
                            _gestureFeedbackLabel.value = "👍 Thumbs Up!"
                            personalityEngine.onPoke()
                            behaviorStateMachine.blink(now)
                            viewModelScope.launch {
                                memoryRepository.recordPoke()
                            }
                            launchFeedbackDismissal()
                        }
                        is HandGestureReaction.FistResolve -> {
                            _gestureFeedbackLabel.value = "✊ Focus Fist!"
                            personalityEngine.onPoke()
                            launchFeedbackDismissal()
                        }
                        is HandGestureReaction.HoldStop -> {
                            _gestureFeedbackLabel.value = if (reaction.isFrozen) "✋ Hold / Stop" else null
                        }
                        is HandGestureReaction.None -> {
                            if (_handGestureReaction.value !is HandGestureReaction.PeaceWink) {
                                _leftEyeOpennessMultiplier.value = 1.0f
                                _rightEyeOpennessMultiplier.value = 1.0f
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchFeedbackDismissal() {
        feedbackDismissalJob?.cancel()
        feedbackDismissalJob = viewModelScope.launch {
            delay(1800L)
            _gestureFeedbackLabel.value = null
        }
    }

    private fun handleVoiceEvent(event: VoiceCommandEvent) {
        when (event) {
            is VoiceCommandEvent.CommandRecognized -> {
                when (val command = event.command) {
                    is VoiceCommand.SwitchMode -> {
                        _currentModeId.value = command.modeId.value
                        viewModelScope.launch {
                            preferencesRepository.setDefaultModeId(command.modeId.value)
                            memoryRepository.recordPoke()
                        }
                        personalityEngine.onPoke()
                    }
                    is VoiceCommand.TriggerAwakening -> {
                        behaviorStateMachine.awaken(System.currentTimeMillis())
                        personalityEngine.onAwakening()
                        viewModelScope.launch {
                            memoryRepository.recordPoke()
                        }
                    }
                    is VoiceCommand.Sleep -> {
                        personalityEngine.onSleep()
                    }
                    is VoiceCommand.Blink -> {
                        behaviorStateMachine.blink(System.currentTimeMillis())
                    }
                    is VoiceCommand.CenterGaze -> {
                        behaviorStateMachine.poke(System.currentTimeMillis())
                    }
                }
            }
            is VoiceCommandEvent.Error -> {
                if (event.isPermissionError) {
                    viewModelScope.launch {
                        preferencesRepository.setVoiceCommandsEnabled(false)
                    }
                }
            }
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceCommandSource?.stopListening()
        handGestureSource?.stop()
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
                        postureObservationSource?.start()
                        handGestureSource?.start()
                    } else {
                        faceObservationSource.stop()
                        postureObservationSource?.stop()
                        handGestureSource?.stop()
                        postureMonitor.reset()
                        headPoseReactionEngine.reset()
                        handGestureReactionEngine.reset()
                        _eyeTiltDegrees.value = 0f
                        _postureAlert.value = PostureAlertState.None
                        _postureObservation.value = PostureObservation.Neutral
                        _activeHandGesture.value = HandObservation.Empty
                        _handGestureReaction.value = HandGestureReaction.None
                        _leftEyeOpennessMultiplier.value = 1.0f
                        _rightEyeOpennessMultiplier.value = 1.0f
                        _gestureFeedbackLabel.value = null
                    }
                }
            }
            is CompanionUiEvent.ToggleDeskMode -> {
                viewModelScope.launch {
                    preferencesRepository.setDeskModeEnabled(event.enabled)
                }
            }
            is CompanionUiEvent.ToggleVoiceCommands -> {
                viewModelScope.launch {
                    preferencesRepository.setVoiceCommandsEnabled(event.enabled)
                }
            }
            is CompanionUiEvent.ToggleHandGestures -> {
                viewModelScope.launch {
                    preferencesRepository.setGestureRecognitionEnabled(event.enabled)
                }
            }
            is CompanionUiEvent.OnStartListeningVoice -> {
                voiceCommandSource?.startListening()
            }
            is CompanionUiEvent.OnStopListeningVoice -> {
                voiceCommandSource?.stopListening()
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
            is CompanionUiEvent.DismissPostureAlert -> {
                postureMonitor.dismissAlert()
                _postureAlert.value = PostureAlertState.None
            }
            is CompanionUiEvent.DismissGestureBadge -> {
                _gestureFeedbackLabel.value = null
            }
        }
    }
}
