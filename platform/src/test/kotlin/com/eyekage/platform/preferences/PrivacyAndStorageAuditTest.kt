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

/**
 * Validates privacy constraints and verifies that the persistence layer
 * only writes strictly approved preference data and never creates unauthorized
 * cached files, raw frames, or biometric records (ADR-002, ADR-005, Req 3.2).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyAndStorageAuditTest {

    private val testJob = SupervisorJob()
    private val testScope = CoroutineScope(Dispatchers.IO + testJob)
    private lateinit var sandboxDir: File
    private lateinit var storeFile: File
    private lateinit var repository: DataStorePreferencesRepository

    private val allowedKeys = setOf(
        "default_mode_id",
        "camera_enabled",
        "reduced_motion_override",
        "haptics_enabled",
        "desk_mode_enabled",
        "voice_commands_enabled",
        "posture_reminder_enabled",
        "posture_slouch_threshold_sec",
        "gesture_recognition_enabled",
        "gesture_sensitivity"
    )

    @Before
    fun setup() {
        val uniqueName = "eyekage_privacy_test_${System.currentTimeMillis()}"
        sandboxDir = File(System.getProperty("java.io.tmpdir"), uniqueName)
        sandboxDir.mkdirs()
        storeFile = File(sandboxDir, "eyekage_settings.preferences_pb")

        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { storeFile }
        )
        repository = DataStorePreferencesRepository(testDataStore)
    }

    @After
    fun teardown() {
        testScope.cancel()
        sandboxDir.deleteRecursively()
    }

    @Test
    fun verifyOnlyAllowlistedKeysAreDefinedInRepository() {
        val companionKeys = listOf(
            DataStorePreferencesRepository.KEY_DEFAULT_MODE_ID.name,
            DataStorePreferencesRepository.KEY_CAMERA_ENABLED.name,
            DataStorePreferencesRepository.KEY_REDUCED_MOTION_OVERRIDE.name,
            DataStorePreferencesRepository.KEY_HAPTICS_ENABLED.name,
            DataStorePreferencesRepository.KEY_DESK_MODE_ENABLED.name,
            DataStorePreferencesRepository.KEY_VOICE_COMMANDS_ENABLED.name,
            DataStorePreferencesRepository.KEY_POSTURE_REMINDER_ENABLED.name,
            DataStorePreferencesRepository.KEY_POSTURE_SLOUCH_THRESHOLD_SEC.name,
            DataStorePreferencesRepository.KEY_GESTURE_RECOGNITION_ENABLED.name,
            DataStorePreferencesRepository.KEY_GESTURE_SENSITIVITY.name
        )

        assertEquals(10, companionKeys.size)
        assertEquals(allowedKeys, companionKeys.toSet())
    }

    @Test
    fun verifyNoUnauthorizedFilesAreCreatedInSandbox() = runTest {
        // Exercise preference writes
        repository.setDefaultModeId("mode.purple_orbit")
        repository.setCameraEnabled(true)
        repository.setReducedMotionOverride("ON")
        repository.setHapticsEnabled(false)
        repository.setDeskModeEnabled(true)
        repository.setVoiceCommandsEnabled(true)
        repository.setPostureReminderEnabled(true)
        repository.setPostureSlouchThresholdSec(15)
        repository.setGestureRecognitionEnabled(true)
        repository.setGestureSensitivity("HIGH")

        // Read all back to ensure I/O flushed
        assertEquals("mode.purple_orbit", repository.defaultModeId.first())
        assertTrue(repository.cameraEnabled.first())
        assertEquals("ON", repository.reducedMotionOverride.first())
        assertFalse(repository.hapticsEnabled.first())
        assertTrue(repository.deskModeEnabled.first())
        assertTrue(repository.voiceCommandsEnabled.first())
        assertTrue(repository.postureReminderEnabled.first())
        assertEquals(15, repository.postureSlouchThresholdSec.first())
        assertTrue(repository.gestureRecognitionEnabled.first())
        assertEquals("HIGH", repository.gestureSensitivity.first())

        val forbiddenImageExts = setOf("jpg", "png", "webp", "bmp", "raw")
        val forbiddenDbExts = setOf("db", "sqlite", "sqlite-wal")
        val forbiddenEmbeddingExts = setOf("bin", "npy", "vec")

        // Verify files on disk in sandbox
        val createdFiles = sandboxDir.walkTopDown().filter { it.isFile }.toList()
        for (file in createdFiles) {
            val extension = file.extension.lowercase()
            assertFalse(file.name, extension in forbiddenImageExts)
            assertFalse(file.name, extension in forbiddenDbExts)
            assertFalse(file.name, extension in forbiddenEmbeddingExts)
            assertTrue(file.name, file.name.startsWith("eyekage_settings"))
        }
    }
}
