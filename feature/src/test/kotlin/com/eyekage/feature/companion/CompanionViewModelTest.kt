package com.eyekage.feature.companion

import com.eyekage.core.behavior.CompanionState
import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.ports.FaceObservationSource
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
class CompanionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePreferencesRepository : PreferencesRepository {
        val defaultModeFlow = MutableStateFlow("mode.normal")
        val cameraEnabledFlow = MutableStateFlow(false)
        val reducedMotionFlow = MutableStateFlow("SYSTEM")
        val hapticsEnabledFlow = MutableStateFlow(true)
        val deskModeFlow = MutableStateFlow(false)
        val voiceCommandsEnabledFlow = MutableStateFlow(false)

        override val defaultModeId: Flow<String> = defaultModeFlow
        override val cameraEnabled: Flow<Boolean> = cameraEnabledFlow
        override val reducedMotionOverride: Flow<String> = reducedMotionFlow
        override val hapticsEnabled: Flow<Boolean> = hapticsEnabledFlow
        override val deskModeEnabled: Flow<Boolean> = deskModeFlow
        override val voiceCommandsEnabled: Flow<Boolean> = voiceCommandsEnabledFlow

        override suspend fun setDefaultModeId(modeId: String) { defaultModeFlow.value = modeId }
        override suspend fun setCameraEnabled(enabled: Boolean) { cameraEnabledFlow.value = enabled }
        override suspend fun setReducedMotionOverride(override: String) { reducedMotionFlow.value = override }
        override suspend fun setHapticsEnabled(enabled: Boolean) { hapticsEnabledFlow.value = enabled }
        override suspend fun setDeskModeEnabled(enabled: Boolean) { deskModeFlow.value = enabled }
        override suspend fun setVoiceCommandsEnabled(enabled: Boolean) { voiceCommandsEnabledFlow.value = enabled }
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
    fun `initial uiState matches default preferences`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("mode.normal", state.currentModeId)
        assertFalse(state.isCameraEnabled)
        assertFalse(state.isCameraTrackingActive)
        assertEquals(CompanionState.Idle, state.behaviorState)
        assertFalse(state.showDebugOverlay)
        assertEquals(1, state.bondTier)
    }

    @Test
    fun `selecting new mode updates uiState and persists to preferences`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.SelectMode("mode.crimson_tracker"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("mode.crimson_tracker", viewModel.uiState.value.currentModeId)
        assertEquals("mode.crimson_tracker", prefs.defaultModeFlow.value)
    }

    @Test
    fun `toggling camera on starts vision pipeline and updates state`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.ToggleCamera(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vision.isRunning.value)
        assertTrue(viewModel.uiState.value.isCameraEnabled)
    }

    @Test
    fun `onPoke event dispatches to behavior state machine and records memory`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.OnPoke(100f, 200f))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.behaviorStateMachine.currentState is CompanionState.Poked)
        assertEquals(1, mem.memoryFlow.value.totalPokes)
    }

    @Test
    fun `toggling debug overlay updates showDebugOverlay in uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showDebugOverlay)

        viewModel.onEvent(CompanionUiEvent.ToggleDebugOverlay)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showDebugOverlay)

        viewModel.onEvent(CompanionUiEvent.ToggleDebugOverlay)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showDebugOverlay)
    }

    @Test
    fun `RecordGazeDuration accumulates gaze seconds in memory repository`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.RecordGazeDuration(5L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5L, mem.memoryFlow.value.totalGazeSeconds)
    }

    @Test
    fun `face observations stream updates faceObservation in uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val mem = FakeMemoryRepository()
        val viewModel = CompanionViewModel(prefs, vision, mem)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val observedFace = FaceObservation(
            normalizedX = 0.45f,
            normalizedY = -0.30f,
            confidence = 0.92f,
            boundingBoxWidth = 0.5f,
            boundingBoxHeight = 0.6f
        )
        vision.observationsFlow.value = observedFace
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(observedFace, viewModel.uiState.value.faceObservation)
        assertTrue(viewModel.uiState.value.faceObservation.isFaceDetected)
    }
}
