package com.eyekage.feature.companion

import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.behavior.HeadPoseReactionEngine
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.behavior.PostureAlertState
import com.eyekage.core.behavior.PostureMonitor
import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.model.HeadPose
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import com.eyekage.core.model.ProximityZone
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.FakePostureObservationSource
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModelPostureTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePreferencesRepository : PreferencesRepository {
        val defaultModeFlow = MutableStateFlow("mode.normal")
        val cameraEnabledFlow = MutableStateFlow(false)
        val reducedMotionFlow = MutableStateFlow("SYSTEM")
        val hapticsEnabledFlow = MutableStateFlow(true)
        val deskModeFlow = MutableStateFlow(false)
        val voiceCommandsEnabledFlow = MutableStateFlow(false)
        val postureReminderEnabledFlow = MutableStateFlow(false)
        val postureSlouchThresholdSecFlow = MutableStateFlow(30)

        override val defaultModeId: Flow<String> = defaultModeFlow
        override val cameraEnabled: Flow<Boolean> = cameraEnabledFlow
        override val reducedMotionOverride: Flow<String> = reducedMotionFlow
        override val hapticsEnabled: Flow<Boolean> = hapticsEnabledFlow
        override val deskModeEnabled: Flow<Boolean> = deskModeFlow
        override val voiceCommandsEnabled: Flow<Boolean> = voiceCommandsEnabledFlow
        override val postureReminderEnabled: Flow<Boolean> = postureReminderEnabledFlow
        override val postureSlouchThresholdSec: Flow<Int> = postureSlouchThresholdSecFlow

        override suspend fun setDefaultModeId(modeId: String) { defaultModeFlow.value = modeId }
        override suspend fun setCameraEnabled(enabled: Boolean) { cameraEnabledFlow.value = enabled }
        override suspend fun setReducedMotionOverride(override: String) { reducedMotionFlow.value = override }
        override suspend fun setHapticsEnabled(enabled: Boolean) { hapticsEnabledFlow.value = enabled }
        override suspend fun setDeskModeEnabled(enabled: Boolean) { deskModeFlow.value = enabled }
        override suspend fun setVoiceCommandsEnabled(enabled: Boolean) { voiceCommandsEnabledFlow.value = enabled }
        override suspend fun setPostureReminderEnabled(enabled: Boolean) { postureReminderEnabledFlow.value = enabled }
        override suspend fun setPostureSlouchThresholdSec(seconds: Int) { postureSlouchThresholdSecFlow.value = seconds }
    }

    private class FakeFaceObservationSource : FaceObservationSource {
        val observationsFlow = MutableStateFlow(FaceObservation.Empty)
        val isRunningFlow = MutableStateFlow(false)

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
    private lateinit var postureSource: FakePostureObservationSource
    private lateinit var reactionEngine: HeadPoseReactionEngine
    private lateinit var postureMonitor: PostureMonitor
    private lateinit var viewModel: CompanionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepo = FakePreferencesRepository()
        faceSource = FakeFaceObservationSource()
        memoryRepo = FakeMemoryRepository()
        postureSource = FakePostureObservationSource()
        reactionEngine = HeadPoseReactionEngine()
        postureMonitor = PostureMonitor(slouchThresholdSeconds = 30)

        viewModel = CompanionViewModel(
            preferencesRepository = preferencesRepo,
            faceObservationSource = faceSource,
            memoryRepository = memoryRepo,
            postureObservationSource = postureSource,
            headPoseReactionEngine = reactionEngine,
            postureMonitor = postureMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun headRollTilt_updatesEyeTiltDegreesInUiState() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0f, viewModel.uiState.value.eyeTiltDegrees, 0.001f)

        // Emit head roll tilt of 14 degrees
        val tiltObs = PostureObservation(
            headPose = HeadPose(pitchDegrees = 0f, yawDegrees = 0f, rollDegrees = 14f),
            proximity = ProximityZone.NORMAL,
            estimatedDistanceRatio = 0.20f,
            postureState = PostureState.TILTED_CURIOUS
        )

        postureSource.emit(tiltObs)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(14f, viewModel.uiState.value.eyeTiltDegrees, 0.001f)

        collectJob.cancel()
    }

    @Test
    fun affirmativeNod_triggersAcknowledgementAndIncrementsMemoryInteraction() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, memoryRepo.memory.value.totalPokes)

        // Pitch dip at t = 1000ms
        postureSource.emit(
            PostureObservation(
                headPose = HeadPose(pitchDegrees = -15f, yawDegrees = 0f, rollDegrees = 0f, timestampNanos = 1_000_000_000L),
                proximity = ProximityZone.NORMAL,
                estimatedDistanceRatio = 0.20f,
                postureState = PostureState.BALANCED,
                timestampNanos = 1_000_000_000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Pitch return at t = 1400ms (400ms later, inside 150-1200ms nod window)
        postureSource.emit(
            PostureObservation(
                headPose = HeadPose(pitchDegrees = 0f, yawDegrees = 0f, rollDegrees = 0f, timestampNanos = 1_400_000_000L),
                proximity = ProximityZone.NORMAL,
                estimatedDistanceRatio = 0.20f,
                postureState = PostureState.BALANCED,
                timestampNanos = 1_400_000_000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, memoryRepo.memory.value.totalPokes)

        collectJob.cancel()
    }

    @Test
    fun sustainedSlouching_triggersPostureAlertWhenReminderEnabled() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        // Enable reminders with 15s threshold
        preferencesRepo.postureReminderEnabledFlow.value = true
        preferencesRepo.postureSlouchThresholdSecFlow.value = 15
        testDispatcher.scheduler.advanceUntilIdle()

        // Configure monitor directly for deterministic timing check
        postureMonitor.slouchThresholdSeconds = 15

        // Start slouching at t = 10s
        postureSource.emit(
            PostureObservation(
                headPose = HeadPose(pitchDegrees = -20f, yawDegrees = 0f, rollDegrees = 0f, timestampNanos = 10_000_000_000L),
                proximity = ProximityZone.NEAR,
                estimatedDistanceRatio = 0.25f,
                postureState = PostureState.SLOUCHING_DOWN,
                timestampNanos = 10_000_000_000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // 20s later at t = 30s (exceeds 15s threshold)
        postureSource.emit(
            PostureObservation(
                headPose = HeadPose(pitchDegrees = -20f, yawDegrees = 0f, rollDegrees = 0f, timestampNanos = 30_000_000_000L),
                proximity = ProximityZone.NEAR,
                estimatedDistanceRatio = 0.25f,
                postureState = PostureState.SLOUCHING_DOWN,
                timestampNanos = 30_000_000_000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.postureAlert is PostureAlertState.AlertActive)

        // Dismiss alert event clears alert
        viewModel.onEvent(CompanionUiEvent.DismissPostureAlert)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PostureAlertState.None, viewModel.uiState.value.postureAlert)

        collectJob.cancel()
    }

    @Test
    fun toggleCameraOff_resetsPostureTrackingAndTilt() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.ToggleCamera(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCameraEnabled)
        assertEquals(0f, viewModel.uiState.value.eyeTiltDegrees, 0.001f)
        assertEquals(PostureAlertState.None, viewModel.uiState.value.postureAlert)

        collectJob.cancel()
    }
}
