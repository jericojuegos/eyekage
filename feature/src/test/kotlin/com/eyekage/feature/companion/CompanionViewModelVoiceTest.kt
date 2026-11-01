package com.eyekage.feature.companion

import com.eyekage.core.behavior.CompanionBehaviorStateMachine
import com.eyekage.core.behavior.CompanionState
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.EyeModeId
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.model.MoodState
import com.eyekage.core.model.VoiceCommand
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.FakeVoiceCommandSource
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModelVoiceTest {

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
    private lateinit var voiceSource: FakeVoiceCommandSource
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var behaviorStateMachine: CompanionBehaviorStateMachine
    private lateinit var viewModel: CompanionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepo = FakePreferencesRepository()
        faceSource = FakeFaceObservationSource()
        memoryRepo = FakeMemoryRepository()
        voiceSource = FakeVoiceCommandSource()
        personalityEngine = PersonalityEngine()
        behaviorStateMachine = CompanionBehaviorStateMachine()

        viewModel = CompanionViewModel(
            preferencesRepository = preferencesRepo,
            faceObservationSource = faceSource,
            memoryRepository = memoryRepo,
            personalityEngine = personalityEngine,
            behaviorStateMachine = behaviorStateMachine,
            voiceCommandSource = voiceSource
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun voiceCommand_switchMode_updatesModeAndRecordsInteraction() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("mode.normal", viewModel.uiState.value.currentModeId)
        assertEquals(0, memoryRepo.memory.value.totalPokes)

        voiceSource.emitCommand(VoiceCommand.SwitchMode(EyeModeId.CRIMSON_TRACKER), "crimson")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("mode.crimson_tracker", viewModel.uiState.value.currentModeId)
        assertEquals("mode.crimson_tracker", preferencesRepo.defaultModeFlow.value)
        assertEquals(1, memoryRepo.memory.value.totalPokes)

        collectJob.cancel()
    }

    @Test
    fun voiceCommand_triggerAwakening_triggersAwakeningReaction() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        voiceSource.emitCommand(VoiceCommand.TriggerAwakening, "awaken")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(behaviorStateMachine.currentState is CompanionState.Awakening)
        assertEquals(1, memoryRepo.memory.value.totalPokes)

        collectJob.cancel()
    }

    @Test
    fun voiceCommand_sleep_transitionsPersonalityMoodToResting() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        voiceSource.emitCommand(VoiceCommand.Sleep, "sleep")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MoodState.DROWSY, personalityEngine.currentMood)

        collectJob.cancel()
    }

    @Test
    fun voiceCommand_blink_triggersBlinkReaction() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        voiceSource.emitCommand(VoiceCommand.Blink, "blink")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(behaviorStateMachine.currentState is CompanionState.Poked)

        collectJob.cancel()
    }

    @Test
    fun preferences_toggleVoiceCommands_startsAndStopsVoiceSource() = runTest {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, voiceSource.startListeningCallCount)

        preferencesRepo.setVoiceCommandsEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, voiceSource.startListeningCallCount)
        assertTrue(voiceSource.isListening.value)

        preferencesRepo.setVoiceCommandsEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, voiceSource.stopListeningCallCount)

        collectJob.cancel()
    }
}
