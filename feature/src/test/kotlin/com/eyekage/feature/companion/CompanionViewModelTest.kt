package com.eyekage.feature.companion

import com.eyekage.core.model.FaceObservation
import com.eyekage.core.ports.FaceObservationSource
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

        override val defaultModeId: Flow<String> = defaultModeFlow
        override val cameraEnabled: Flow<Boolean> = cameraEnabledFlow
        override val reducedMotionOverride: Flow<String> = reducedMotionFlow
        override val hapticsEnabled: Flow<Boolean> = hapticsEnabledFlow
        override val deskModeEnabled: Flow<Boolean> = deskModeFlow

        override suspend fun setDefaultModeId(modeId: String) { defaultModeFlow.value = modeId }
        override suspend fun setCameraEnabled(enabled: Boolean) { cameraEnabledFlow.value = enabled }
        override suspend fun setReducedMotionOverride(override: String) { reducedMotionFlow.value = override }
        override suspend fun setHapticsEnabled(enabled: Boolean) { hapticsEnabledFlow.value = enabled }
        override suspend fun setDeskModeEnabled(enabled: Boolean) { deskModeFlow.value = enabled }
    }

    private class FakeFaceObservationSource : FaceObservationSource {
        val observationsFlow = MutableStateFlow(FaceObservation.Empty)
        val isRunningFlow = MutableStateFlow(false)

        override val observations: StateFlow<FaceObservation> = observationsFlow.asStateFlow()
        override val isRunning: StateFlow<Boolean> = isRunningFlow.asStateFlow()

        override fun start() { isRunningFlow.value = true }
        override fun stop() { isRunningFlow.value = false }
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
        val viewModel = CompanionViewModel(prefs, vision)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("mode.normal", state.currentModeId)
        assertFalse(state.isCameraEnabled)
        assertFalse(state.isCameraTrackingActive)
    }

    @Test
    fun `selecting new mode updates uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val viewModel = CompanionViewModel(prefs, vision)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.SelectMode("mode.crimson_tracker"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("mode.crimson_tracker", viewModel.uiState.value.currentModeId)
    }

    @Test
    fun `toggling camera on starts vision pipeline and updates state`() = runTest {
        val prefs = FakePreferencesRepository()
        val vision = FakeFaceObservationSource()
        val viewModel = CompanionViewModel(prefs, vision)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CompanionUiEvent.ToggleCamera(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vision.isRunning.value)
        assertTrue(viewModel.uiState.value.isCameraEnabled)
    }
}
