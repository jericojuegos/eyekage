package com.eyekage.feature.settings

import com.eyekage.core.modes.DefaultModeCatalog
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val viewModel = SettingsViewModel(prefs)

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
        assertEquals(DefaultModeCatalog.getSelectableModes().size, state.availableModes.size)
    }

    @Test
    fun `SetDefaultMode updates preference repository and uiState`() = runTest {
        val prefs = FakePreferencesRepository()
        val viewModel = SettingsViewModel(prefs)

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
        val viewModel = SettingsViewModel(prefs)

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
        val viewModel = SettingsViewModel(prefs)

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
        val viewModel = SettingsViewModel(prefs)

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
        val viewModel = SettingsViewModel(prefs)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetDeskModeEnabled(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.deskModeFlow.value)
        assertTrue(viewModel.uiState.value.isDeskModeEnabled)
    }
}
