package com.eyekage.feature.settings

import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.HeadPose
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import com.eyekage.core.model.ProximityZone
import com.eyekage.core.modes.DefaultModeCatalog
import com.eyekage.core.ports.FakePostureObservationSource
import com.eyekage.core.ports.MemoryRepository
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class SettingsViewModelTest {

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

    private class FakeMemoryRepository : MemoryRepository {
        val memoryFlow = MutableStateFlow(CompanionMemory())
        override val memory: StateFlow<CompanionMemory> = memoryFlow

        override suspend fun recordPoke() {
            val curr = memoryFlow.value
            memoryFlow.value = curr.copy(totalPokes = curr.totalPokes + 1, lastInteractionTimestamp = 1000L)
        }

        override suspend fun recordGazeDuration(seconds: Long) {
            val curr = memoryFlow.value
            val newSeconds = curr.totalGazeSeconds + seconds
            memoryFlow.value = curr.copy(totalGazeSeconds = newSeconds, lastInteractionTimestamp = 1000L)
        }

        override suspend fun clearMemory() {
            memoryFlow.value = CompanionMemory()
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState matches default preferences and provides selectable modes`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("mode.normal", state.defaultModeId)
        assertFalse(state.isCameraEnabled)
        assertEquals("SYSTEM", state.reducedMotionOverride)
        assertTrue(state.isHapticsEnabled)
        assertFalse(state.isDeskModeEnabled)
        assertFalse(state.isVoiceCommandsEnabled)
        assertFalse(state.isPostureReminderEnabled)
        assertEquals(30, state.postureSlouchThresholdSec)
        assertEquals(PostureState.BALANCED, state.currentPostureState)
        assertEquals(DefaultModeCatalog.getSelectableModes().size, state.availableModes.size)
        assertEquals(1, state.memory.bondTier)
        assertEquals(0, state.memory.totalPokes)
    }

    @Test
    fun `ResetMemory restores companion memory to defaults`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        mem.recordPoke()
        mem.recordGazeDuration(300L)

        val viewModel = SettingsViewModel(prefs, mem)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.memory.totalPokes)
        assertEquals(300L, viewModel.uiState.value.memory.totalGazeSeconds)

        viewModel.onEvent(SettingsUiEvent.ResetMemory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.memory.totalPokes)
        assertEquals(0L, viewModel.uiState.value.memory.totalGazeSeconds)
        assertEquals(1, viewModel.uiState.value.memory.bondTier)
    }

    @Test
    fun `SetDefaultMode updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetDefaultMode("mode.crimson_tracker"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("mode.crimson_tracker", prefs.defaultModeFlow.value)
        assertEquals("mode.crimson_tracker", viewModel.uiState.value.defaultModeId)
    }

    @Test
    fun `SetCameraEnabled updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetCameraEnabled(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.cameraEnabledFlow.value)
        assertTrue(viewModel.uiState.value.isCameraEnabled)
    }

    @Test
    fun `SetReducedMotion updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetReducedMotion("ON"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("ON", prefs.reducedMotionFlow.value)
        assertEquals("ON", viewModel.uiState.value.reducedMotionOverride)
    }

    @Test
    fun `SetHapticsEnabled updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetHapticsEnabled(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(prefs.hapticsEnabledFlow.value)
        assertFalse(viewModel.uiState.value.isHapticsEnabled)
    }

    @Test
    fun `SetDeskModeEnabled updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetDeskModeEnabled(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.deskModeFlow.value)
        assertTrue(viewModel.uiState.value.isDeskModeEnabled)
    }

    @Test
    fun `SetVoiceCommandsEnabled updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isVoiceCommandsEnabled)

        viewModel.onEvent(SettingsUiEvent.SetVoiceCommandsEnabled(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.voiceCommandsEnabledFlow.value)
        assertTrue(viewModel.uiState.value.isVoiceCommandsEnabled)

        viewModel.onEvent(SettingsUiEvent.SetVoiceCommandsEnabled(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(prefs.voiceCommandsEnabledFlow.value)
        assertFalse(viewModel.uiState.value.isVoiceCommandsEnabled)
    }

    @Test
    fun `SetPostureReminderEnabled updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPostureReminderEnabled)

        viewModel.onEvent(SettingsUiEvent.SetPostureReminderEnabled(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.postureReminderEnabledFlow.value)
        assertTrue(viewModel.uiState.value.isPostureReminderEnabled)

        viewModel.onEvent(SettingsUiEvent.SetPostureReminderEnabled(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(prefs.postureReminderEnabledFlow.value)
        assertFalse(viewModel.uiState.value.isPostureReminderEnabled)
    }

    @Test
    fun `SetPostureSlouchThreshold updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(30, viewModel.uiState.value.postureSlouchThresholdSec)

        viewModel.onEvent(SettingsUiEvent.SetPostureSlouchThreshold(15))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(15, prefs.postureSlouchThresholdSecFlow.value)
        assertEquals(15, viewModel.uiState.value.postureSlouchThresholdSec)

        viewModel.onEvent(SettingsUiEvent.SetPostureSlouchThreshold(45))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(45, prefs.postureSlouchThresholdSecFlow.value)
        assertEquals(45, viewModel.uiState.value.postureSlouchThresholdSec)
    }

    @Test
    fun `PostureObservation updates currentPostureState in uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val fakePosture = FakePostureObservationSource()
        val viewModel = SettingsViewModel(
            preferencesRepository = prefs,
            memoryRepository = mem,
            postureObservationSource = fakePosture
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PostureState.BALANCED, viewModel.uiState.value.currentPostureState)

        fakePosture.emit(
            PostureObservation(
                headPose = HeadPose(pitchDegrees = 30f, yawDegrees = 0f, rollDegrees = 0f, timestampNanos = 1000L),
                proximity = ProximityZone.NEAR,
                estimatedDistanceRatio = 0.4f,
                postureState = PostureState.SLOUCHING_DOWN
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PostureState.SLOUCHING_DOWN, viewModel.uiState.value.currentPostureState)
    }

    @Test
    fun `SetGestureRecognitionEnabled updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isGestureRecognitionEnabled)

        viewModel.onEvent(SettingsUiEvent.SetGestureRecognitionEnabled(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(prefs.gestureRecognitionEnabledFlow.value)
        assertFalse(viewModel.uiState.value.isGestureRecognitionEnabled)
    }

    @Test
    fun `SetGestureSensitivity updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val viewModel = SettingsViewModel(prefs, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("NORMAL", viewModel.uiState.value.gestureSensitivity)

        viewModel.onEvent(SettingsUiEvent.SetGestureSensitivity("HIGH"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("HIGH", prefs.gestureSensitivityFlow.value)
        assertEquals("HIGH", viewModel.uiState.value.gestureSensitivity)
    }

    @Test
    fun `HandObservation updates currentHandGesture in uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val mem = FakeMemoryRepository()
        val fakeGesture = com.eyekage.core.ports.FakeHandGestureSource()
        val viewModel = SettingsViewModel(
            preferencesRepository = prefs,
            memoryRepository = mem,
            handGestureSource = fakeGesture
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(com.eyekage.core.model.HandObservation.Empty, viewModel.uiState.value.currentHandGesture)

        val obs = com.eyekage.core.model.HandObservation(
            gesture = com.eyekage.core.model.HandGesture.PEACE_SIGN,
            confidence = 0.88f,
            normalizedCenterX = 0.5f,
            normalizedCenterY = 0.5f,
            boundingBoxArea = 0.12f
        )
        fakeGesture.emit(obs)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(obs, viewModel.uiState.value.currentHandGesture)
    }
}
