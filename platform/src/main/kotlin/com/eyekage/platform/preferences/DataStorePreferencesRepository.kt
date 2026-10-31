package com.eyekage.platform.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eyekage.core.ports.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eyekage_settings")

/**
 * AndroidX Preferences DataStore implementation of [PreferencesRepository].
 * Strictly enforces only the 5 approved keys.
 */
class DataStorePreferencesRepository(
    private val context: Context
) : PreferencesRepository {

    private object Keys {
        val DEFAULT_MODE_ID = stringPreferencesKey("default_mode_id")
        val CAMERA_ENABLED = booleanPreferencesKey("camera_enabled")
        val REDUCED_MOTION_OVERRIDE = stringPreferencesKey("reduced_motion_override")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val DESK_MODE_ENABLED = booleanPreferencesKey("desk_mode_enabled")
    }

    override val defaultModeId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.DEFAULT_MODE_ID] ?: "mode.normal"
    }

    override val cameraEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.CAMERA_ENABLED] ?: false
    }

    override val reducedMotionOverride: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.REDUCED_MOTION_OVERRIDE] ?: "SYSTEM"
    }

    override val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.HAPTICS_ENABLED] ?: true
    }

    override val deskModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.DESK_MODE_ENABLED] ?: false
    }

    override suspend fun setDefaultModeId(modeId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_MODE_ID] = modeId
        }
    }

    override suspend fun setCameraEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CAMERA_ENABLED] = enabled
        }
    }

    override suspend fun setReducedMotionOverride(override: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.REDUCED_MOTION_OVERRIDE] = override
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    override suspend fun setDeskModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DESK_MODE_ENABLED] = enabled
        }
    }
}
