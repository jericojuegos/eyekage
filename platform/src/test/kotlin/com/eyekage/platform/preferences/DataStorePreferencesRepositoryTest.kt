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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStorePreferencesRepositoryTest {

    private val dataStoreJob = SupervisorJob()
    private val dataStoreScope = CoroutineScope(Dispatchers.IO + dataStoreJob)
    private lateinit var tempFile: File
    private lateinit var repository: DataStorePreferencesRepository

    @Before
    fun setup() {
        tempFile = File.createTempFile("test_preferences", ".preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tempFile }
        )
        repository = DataStorePreferencesRepository(testDataStore)
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
        assertEquals(DataStorePreferencesRepository.DEFAULT_MODE_ID_VALUE, repository.defaultModeId.first())
        assertFalse(repository.cameraEnabled.first())
        val defaultMotion = DataStorePreferencesRepository.DEFAULT_REDUCED_MOTION_VALUE
        assertEquals(defaultMotion, repository.reducedMotionOverride.first())
        assertTrue(repository.hapticsEnabled.first())
        assertFalse(repository.deskModeEnabled.first())
    }

    @Test
    fun updateDefaultModeId_persistsAndEmitsNewValue() = runTest {
        assertEquals("mode.normal", repository.defaultModeId.first())
        repository.setDefaultModeId("mode.crimson_tracker")
        assertEquals("mode.crimson_tracker", repository.defaultModeId.first())
    }

    @Test
    fun updateCameraEnabled_persistsAndEmitsNewValue() = runTest {
        assertFalse(repository.cameraEnabled.first())
        repository.setCameraEnabled(true)
        assertTrue(repository.cameraEnabled.first())
    }

    @Test
    fun updateReducedMotionOverride_persistsAndEmitsNewValue() = runTest {
        assertEquals("SYSTEM", repository.reducedMotionOverride.first())
        repository.setReducedMotionOverride("ON")
        assertEquals("ON", repository.reducedMotionOverride.first())
        repository.setReducedMotionOverride("OFF")
        assertEquals("OFF", repository.reducedMotionOverride.first())
    }

    @Test
    fun updateHapticsEnabled_persistsAndEmitsNewValue() = runTest {
        assertTrue(repository.hapticsEnabled.first())
        repository.setHapticsEnabled(false)
        assertFalse(repository.hapticsEnabled.first())
    }

    @Test
    fun updateDeskModeEnabled_persistsAndEmitsNewValue() = runTest {
        assertFalse(repository.deskModeEnabled.first())
        repository.setDeskModeEnabled(true)
        assertTrue(repository.deskModeEnabled.first())
    }
}
