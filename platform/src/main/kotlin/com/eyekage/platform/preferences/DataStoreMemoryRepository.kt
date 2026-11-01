package com.eyekage.platform.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.ports.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.memoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "eyekage_memory")

/**
 * AndroidX Preferences DataStore implementation of [MemoryRepository].
 * Manages the 4 persistent memory keys: total pokes, total gaze tracking duration,
 * bond tier, and last interaction timestamp.
 */
class DataStoreMemoryRepository(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val personalityEngine: PersonalityEngine = PersonalityEngine()
) : MemoryRepository {

    constructor(context: Context) : this(context.memoryDataStore)

    companion object {
        val KEY_TOTAL_POKES = intPreferencesKey("mem_total_pokes")
        val KEY_TOTAL_GAZE_SECONDS = longPreferencesKey("mem_total_gaze_seconds")
        val KEY_BOND_TIER = intPreferencesKey("mem_bond_tier")
        val KEY_LAST_INTERACTION_TS = longPreferencesKey("mem_last_interaction_ts")

        const val DEFAULT_TOTAL_POKES = 0
        const val DEFAULT_TOTAL_GAZE_SECONDS = 0L
        const val DEFAULT_BOND_TIER = 1
        const val DEFAULT_LAST_INTERACTION_TS = 0L
    }

    override val memory: StateFlow<CompanionMemory> = dataStore.data.map { preferences ->
        CompanionMemory(
            totalPokes = preferences[KEY_TOTAL_POKES] ?: DEFAULT_TOTAL_POKES,
            totalGazeSeconds = preferences[KEY_TOTAL_GAZE_SECONDS] ?: DEFAULT_TOTAL_GAZE_SECONDS,
            bondTier = preferences[KEY_BOND_TIER] ?: DEFAULT_BOND_TIER,
            lastInteractionTimestamp = preferences[KEY_LAST_INTERACTION_TS] ?: DEFAULT_LAST_INTERACTION_TS
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = CompanionMemory()
    )

    override suspend fun recordPoke() {
        val now = System.currentTimeMillis()
        dataStore.edit { preferences ->
            val pokes = (preferences[KEY_TOTAL_POKES] ?: DEFAULT_TOTAL_POKES) + 1
            preferences[KEY_TOTAL_POKES] = pokes
            preferences[KEY_LAST_INTERACTION_TS] = now
        }
    }

    override suspend fun recordGazeDuration(seconds: Long) {
        if (seconds <= 0L) return
        val now = System.currentTimeMillis()
        dataStore.edit { preferences ->
            val currentGaze = (preferences[KEY_TOTAL_GAZE_SECONDS] ?: DEFAULT_TOTAL_GAZE_SECONDS) + seconds
            preferences[KEY_TOTAL_GAZE_SECONDS] = currentGaze
            preferences[KEY_BOND_TIER] = personalityEngine.computeBondTier(currentGaze)
            preferences[KEY_LAST_INTERACTION_TS] = now
        }
    }

    override suspend fun clearMemory() {
        dataStore.edit { preferences ->
            preferences[KEY_TOTAL_POKES] = DEFAULT_TOTAL_POKES
            preferences[KEY_TOTAL_GAZE_SECONDS] = DEFAULT_TOTAL_GAZE_SECONDS
            preferences[KEY_BOND_TIER] = DEFAULT_BOND_TIER
            preferences[KEY_LAST_INTERACTION_TS] = DEFAULT_LAST_INTERACTION_TS
        }
    }
}
