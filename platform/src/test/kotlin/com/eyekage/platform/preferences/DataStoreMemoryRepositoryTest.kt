package com.eyekage.platform.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreMemoryRepositoryTest {

    private val dataStoreJob = SupervisorJob()
    private val dataStoreScope = CoroutineScope(Dispatchers.IO + dataStoreJob)
    private lateinit var tempFile: File
    private lateinit var repository: DataStoreMemoryRepository

    @Before
    fun setup() {
        tempFile = File.createTempFile("test_memory", ".preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tempFile }
        )
        repository = DataStoreMemoryRepository(
            dataStore = testDataStore,
            scope = dataStoreScope
        )
    }

    @After
    fun teardown() {
        dataStoreScope.cancel()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    @Test
    fun defaultValues_areReturned_whenDataStoreIsEmpty() = runTest {
        val memory = repository.memory.first()
        assertEquals(0, memory.totalPokes)
        assertEquals(0L, memory.totalGazeSeconds)
        assertEquals(1, memory.bondTier)
        assertEquals(0L, memory.lastInteractionTimestamp)
    }

    @Test
    fun recordPoke_incrementsPokeCountAndUpdatesTimestamp() = runTest {
        repository.recordPoke()
        repository.recordPoke()

        val memory = repository.memory.first { it.totalPokes == 2 }
        assertEquals(2, memory.totalPokes)
        assertTrue(memory.lastInteractionTimestamp > 0L)
    }

    @Test
    fun recordGazeDuration_accumulatesSecondsAndAdvancesBondTier() = runTest {
        // Record 300 seconds -> Bond Tier 2
        repository.recordGazeDuration(300L)

        val memoryTier2 = repository.memory.first { it.totalGazeSeconds == 300L }
        assertEquals(300L, memoryTier2.totalGazeSeconds)
        assertEquals(2, memoryTier2.bondTier)

        // Record another 600 seconds -> 900 seconds -> Bond Tier 3
        repository.recordGazeDuration(600L)
        val memoryTier3 = repository.memory.first { it.totalGazeSeconds == 900L }
        assertEquals(900L, memoryTier3.totalGazeSeconds)
        assertEquals(3, memoryTier3.bondTier)
    }

    @Test
    fun clearMemory_resetsAllKeysToInitialDefaultState() = runTest {
        repository.recordPoke()
        repository.recordGazeDuration(1800L)

        val beforeReset = repository.memory.first { it.totalPokes > 0 }
        assertEquals(1, beforeReset.totalPokes)
        assertEquals(1800L, beforeReset.totalGazeSeconds)
        assertEquals(4, beforeReset.bondTier)

        repository.clearMemory()

        val afterReset = repository.memory.first { it.totalPokes == 0 }
        assertEquals(0, afterReset.totalPokes)
        assertEquals(0L, afterReset.totalGazeSeconds)
        assertEquals(1, afterReset.bondTier)
        assertEquals(0L, afterReset.lastInteractionTimestamp)
    }
}
