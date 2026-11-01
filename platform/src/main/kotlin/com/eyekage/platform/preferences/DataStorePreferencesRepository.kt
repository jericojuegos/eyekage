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
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    constructor(context: Context) : this(context.dataStore)

    companion object {
        val KEY_DEFAULT_MODE_ID = stringPreferencesKey("default_mode_id")
        val KEY_CAMERA_ENABLED = booleanPreferencesKey("camera_enabled")
        val KEY_REDUCED_MOTION_OVERRIDE = stringPreferencesKey("reduced_motion_override")
        val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEY_DESK_MODE_ENABLED = booleanPreferencesKey("desk_mode_enabled")

        const val DEFAULT_MODE_ID_VALUE = "mode.normal"
        const val DEFAULT_CAMERA_ENABLED_VALUE = false
        const val DEFAULT_REDUCED_MOTION_VALUE = "SYSTEM"
        const val DEFAULT_HAPTICS_ENABLED_VALUE = true
        const val DEFAULT_DESK_MODE_ENABLED_VALUE = false
    }

    override val defaultModeId: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_MODE_ID] ?: DEFAULT_MODE_ID_VALUE
    }

    override val cameraEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_CAMERA_ENABLED] ?: DEFAULT_CAMERA_ENABLED_VALUE
    }

    override val reducedMotionOverride: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_REDUCED_MOTION_OVERRIDE] ?: DEFAULT_REDUCED_MOTION_VALUE
    }

    override val hapticsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_HAPTICS_ENABLED] ?: DEFAULT_HAPTICS_ENABLED_VALUE
    }

    override val deskModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DESK_MODE_ENABLED] ?: DEFAULT_DESK_MODE_ENABLED_VALUE
    }

    override suspend fun setDefaultModeId(modeId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_MODE_ID] = modeId
        }
    }

    override suspend fun setCameraEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_CAMERA_ENABLED] = enabled
        }
    }

    override suspend fun setReducedMotionOverride(override: String) {
        dataStore.edit { preferences ->
            preferences[KEY_REDUCED_MOTION_OVERRIDE] = override
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTICS_ENABLED] = enabled
        }
    }

    override suspend fun setDeskModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DESK_MODE_ENABLED] = enabled
        }
    }
}
