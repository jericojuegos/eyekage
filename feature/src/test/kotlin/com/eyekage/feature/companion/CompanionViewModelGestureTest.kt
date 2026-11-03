package com.eyekage.feature.companion

import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.behavior.HandGestureReactionEngine
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandGestureReaction
import com.eyekage.core.model.HandObservation
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.FakeHandGestureSource
import com.eyekage.core.ports.MemoryRepository
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModelGestureTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePreferencesRepository : PreferencesRepository {
        val defaultModeFlow = MutableStateFlow("mode.normal")
        val cameraEnabledFlow = MutableStateFlow(true)
        val reducedMotionFlow = MutableStateFlow("SYSTEM")
        val hapticsEnabledFlow = MutableStateFlow(true)
        val deskModeFlow = MutableStateFlow(false)
        val voiceCommandsEnabledFlow = MutableStateFlow(false)
        val postureReminderEnabledFlow = MutableStateFlow(false)
        val postureSlouchThresholdSecFlow = MutableStateFlow(30)
        val gestureRecognitionEnabledFlow = MutableStateFlow(true)
        val gestureSensitivityFlow = MutableStateFlow("NORMAL")

        override val defaultModeId: Flow<String> = defaultModeFlow
        override val cameraEnabled: Flow<Boolean> = cameraEnabledFlow
        override val reducedMotionOverride: Flow<String> = reducedMotionFlow
        override val hapticsEnabled: Flow<Boolean> = hapticsEnabledFlow
        override val deskModeEnabled: Flow<Boolean> = deskModeFlow
        override val voiceCommandsEnabled: Flow<Boolean> = voiceCommandsEnabledFlow
        override val postureReminderEnabled: Flow<Boolean> = postureReminderEnabledFlow
        override val postureSlouchThresholdSec: Flow<Int> = postureSlouchThresholdSecFlow
        override val gestureRecognitionEnabled: Flow<Boolean> = gestureRecognitionEnabledFlow
        override val gestureSensitivity: Flow<String> = gestureSensitivityFlow

        override suspend fun setDefaultModeId(modeId: String) { defaultModeFlow.value = modeId }
        override suspend fun setCameraEnabled(enabled: Boolean) { cameraEnabledFlow.value = enabled }
        override suspend fun setReducedMotionOverride(override: String) { reducedMotionFlow.value = override }
        override suspend fun setHapticsEnabled(enabled: Boolean) { hapticsEnabledFlow.value = enabled }
        override suspend fun setDeskModeEnabled(enabled: Boolean) { deskModeFlow.value = enabled }
        override suspend fun setVoiceCommandsEnabled(enabled: Boolean) { voiceCommandsEnabledFlow.value = enabled }
        override suspend fun setPostureReminderEnabled(enabled: Boolean) { postureReminderEnabledFlow.value = enabled }
        override suspend fun setPostureSlouchThresholdSec(seconds: Int) { postureSlouchThresholdSecFlow.value = seconds }
        override suspend fun setGestureRecognitionEnabled(enabled: Boolean) { gestureRecognitionEnabledFlow.value = enabled }
        override suspend fun setGestureSensitivity(sensitivity: String) { gestureSensitivityFlow.value = sensitivity }
    }

    private class FakeFaceObservationSource : FaceObservationSource {
        val observationsFlow = MutableStateFlow(FaceObservation.Empty)
        val isRunningFlow = MutableStateFlow(true)

        override val observations: StateFlow<FaceObservation> = observationsFlow.asStateFlow()
        override val isRunning: StateFlow<Boolean> = isRunningFlow.asStateFlow()

        override fun start() { isRunningFlow.value = true }
        override fun stop() { isRunningFlow.value = false }
    }

    private class FakeMemoryRepository : MemoryRepository {
        val memoryFlow = MutableStateFlow(CompanionMemory())
        override val memory: StateFlow<CompanionMemory> = memoryFlow.asStateFlow()

        override suspend fun recordPoke() {
            val curr = memoryFlow.value
            memoryFlow.value = curr.copy(totalPokes = curr.totalPokes + 1)
        }

        override suspend fun recordGazeDuration(seconds: Long) {
            val curr = memoryFlow.value
            memoryFlow.value = curr.copy(totalGazeSeconds = curr.totalGazeSeconds + seconds)
        }

        override suspend fun clearMemory() {
            memoryFlow.value = CompanionMemory()
        }
    }

    private lateinit var preferencesRepo: FakePreferencesRepository
    private lateinit var faceSource: FakeFaceObservationSource
    private lateinit var memoryRepo: FakeMemoryRepository
    private lateinit var gestureSource: FakeHandGestureSource
    private lateinit var reactionEngine: HandGestureReactionEngine
    private lateinit var viewModel: CompanionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepo = FakePreferencesRepository()
        faceSource = FakeFaceObservationSource()
        memoryRepo = FakeMemoryRepository()
        gestureSource = FakeHandGestureSource()
        reactionEngine = HandGestureReactionEngine()

        viewModel = CompanionViewModel(
            preferencesRepository = preferencesRepo,
            faceObservationSource = faceSource,
            memoryRepository = memoryRepo,
            personalityEngine = PersonalityEngine(),
            behaviorStateMachine = CompanionBehaviorStateMachine(),
            handGestureSource = gestureSource,
            handGestureReactionEngine = reactionEngine
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun waveGesture_triggersWaveGreeting_incrementsMemoryAndEmitsFeedbackLabel() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, memoryRepo.memory.value.totalPokes)
        assertNull(viewModel.uiState.value.activeGestureFeedbackLabel)

        // Wave initiation at t = 1000ms
        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.WAVE,
                confidence = 0.90f,
                normalizedCenterX = 0.3f,
                normalizedCenterY = 0.4f,
                boundingBoxArea = 0.15f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        // Confirmation at t = 1300ms (300ms duration >= 250ms normal window)
        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.WAVE,
                confidence = 0.92f,
                normalizedCenterX = 0.3f,
                normalizedCenterY = 0.4f,
                boundingBoxArea = 0.15f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals("👋 Wave Detected!", viewModel.uiState.value.activeGestureFeedbackLabel)
        assertEquals(1, memoryRepo.memory.value.totalPokes)
        assertTrue(viewModel.uiState.value.handGestureReaction is HandGestureReaction.WaveGreeting)

        // Verify auto-dismissal after 1800ms
        testDispatcher.scheduler.advanceTimeBy(1900L)
        testDispatcher.scheduler.runCurrent()
        assertNull(viewModel.uiState.value.activeGestureFeedbackLabel)

        collectJob.cancel()
    }

    @Test
    fun peaceGesture_triggersPeaceWink_setsEyeOpennessMultiplierAndEmitsLabel() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.runCurrent()

        // Peace sign initiation on left side (x = -0.4f => left eye winks)
        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.PEACE_SIGN,
                confidence = 0.88f,
                normalizedCenterX = -0.4f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.12f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.PEACE_SIGN,
                confidence = 0.90f,
                normalizedCenterX = -0.4f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.12f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals("✌️ Peace Sign!", viewModel.uiState.value.activeGestureFeedbackLabel)
        assertTrue(viewModel.uiState.value.handGestureReaction is HandGestureReaction.PeaceWink)
        assertEquals(0.0f, viewModel.uiState.value.leftEyeOpennessMultiplier, 0.001f)
        assertEquals(1.0f, viewModel.uiState.value.rightEyeOpennessMultiplier, 0.001f)

        // Advance past wink recovery duration (600ms)
        testDispatcher.scheduler.advanceTimeBy(700L)
        testDispatcher.scheduler.runCurrent()

        assertEquals(1.0f, viewModel.uiState.value.leftEyeOpennessMultiplier, 0.001f)
        assertEquals(1.0f, viewModel.uiState.value.rightEyeOpennessMultiplier, 0.001f)

        // Advance past feedback label auto-dismissal (1800ms)
        testDispatcher.scheduler.advanceTimeBy(1200L)
        testDispatcher.scheduler.runCurrent()
        assertNull(viewModel.uiState.value.activeGestureFeedbackLabel)

        collectJob.cancel()
    }

    @Test
    fun thumbsUpGesture_triggersApproval_incrementsMemoryAndEmitsLabel() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.THUMBS_UP,
                confidence = 0.95f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.10f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.THUMBS_UP,
                confidence = 0.95f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.10f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals("👍 Thumbs Up!", viewModel.uiState.value.activeGestureFeedbackLabel)
        assertEquals(1, memoryRepo.memory.value.totalPokes)
        assertTrue(viewModel.uiState.value.handGestureReaction is HandGestureReaction.ThumbsUpApproval)

        collectJob.cancel()
    }

    @Test
    fun fistGesture_triggersFistResolve_emitsLabel() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.CLOSED_FIST,
                confidence = 0.90f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.10f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.CLOSED_FIST,
                confidence = 0.91f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.10f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals("✊ Focus Fist!", viewModel.uiState.value.activeGestureFeedbackLabel)
        assertTrue(viewModel.uiState.value.handGestureReaction is HandGestureReaction.FistResolve)

        collectJob.cancel()
    }

    @Test
    fun holdGesture_triggersHoldStop_emitsHoldLabel() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.FLAT_PALM_STOP,
                confidence = 0.92f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.20f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.FLAT_PALM_STOP,
                confidence = 0.93f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.20f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals("✋ Hold / Stop", viewModel.uiState.value.activeGestureFeedbackLabel)
        val reaction = viewModel.uiState.value.handGestureReaction
        assertTrue(reaction is HandGestureReaction.HoldStop)
        assertTrue((reaction as HandGestureReaction.HoldStop).isFrozen)

        collectJob.cancel()
    }

    @Test
    fun dismissGestureBadge_clearsFeedbackLabel() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.WAVE,
                confidence = 0.90f,
                normalizedCenterX = 0.3f,
                normalizedCenterY = 0.4f,
                boundingBoxArea = 0.15f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.WAVE,
                confidence = 0.90f,
                normalizedCenterX = 0.3f,
                normalizedCenterY = 0.4f,
                boundingBoxArea = 0.15f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals("👋 Wave Detected!", viewModel.uiState.value.activeGestureFeedbackLabel)

        viewModel.onEvent(CompanionUiEvent.DismissGestureBadge)
        testDispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.value.activeGestureFeedbackLabel)

        collectJob.cancel()
    }

    @Test
    fun toggleHandGestures_updatesPreference() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(preferencesRepo.gestureRecognitionEnabledFlow.value)

        viewModel.onEvent(CompanionUiEvent.ToggleHandGestures(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, preferencesRepo.gestureRecognitionEnabledFlow.value)

        collectJob.cancel()
    }

    @Test
    fun whenDisabled_gestureIgnored() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        preferencesRepo.gestureRecognitionEnabledFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.WAVE,
                confidence = 0.95f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.20f,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        gestureSource.emit(
            HandObservation(
                gesture = HandGesture.WAVE,
                confidence = 0.95f,
                normalizedCenterX = 0.5f,
                normalizedCenterY = 0.5f,
                boundingBoxArea = 0.20f,
                timestampNanos = 1_300_000_000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.activeGestureFeedbackLabel)
        assertEquals(0, memoryRepo.memory.value.totalPokes)

        collectJob.cancel()
    }
}
