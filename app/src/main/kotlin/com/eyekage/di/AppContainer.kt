package com.eyekage.di

import android.content.Context
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.PreferencesRepository
import com.eyekage.platform.preferences.DataStorePreferencesRepository
import com.eyekage.platform.vision.VisionFaceObservationSource

/**
 * Manual Dependency Injection Container (ADR-007).
 * Pure-Kotlin composition root with zero annotation processors and zero reflection.
 */
interface AppContainer {
    val preferencesRepository: PreferencesRepository
    val faceObservationSource: FaceObservationSource
}

class DefaultAppContainer(
    private val appContext: Context
) : AppContainer {

    override val preferencesRepository: PreferencesRepository by lazy {
        DataStorePreferencesRepository(appContext)
    }

    override val faceObservationSource: FaceObservationSource by lazy {
        VisionFaceObservationSource(appContext)
    }
}
