package com.eyekage.core.ports

import kotlinx.coroutines.flow.Flow

/**
 * Port for accessing persisted app settings.
 * Only the 5 approved Phase 1 keys are permitted.
 */
interface PreferencesRepository {
    val defaultModeId: Flow<String>
    val cameraEnabled: Flow<Boolean>
    val reducedMotionOverride: Flow<String>
    val hapticsEnabled: Flow<Boolean>
    val deskModeEnabled: Flow<Boolean>
    val voiceCommandsEnabled: Flow<Boolean>
    val postureReminderEnabled: Flow<Boolean>
    val postureSlouchThresholdSec: Flow<Int>

    suspend fun setDefaultModeId(modeId: String)
    suspend fun setCameraEnabled(enabled: Boolean)
    suspend fun setReducedMotionOverride(override: String)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setDeskModeEnabled(enabled: Boolean)
    suspend fun setVoiceCommandsEnabled(enabled: Boolean)
    suspend fun setPostureReminderEnabled(enabled: Boolean)
    suspend fun setPostureSlouchThresholdSec(seconds: Int)
}
