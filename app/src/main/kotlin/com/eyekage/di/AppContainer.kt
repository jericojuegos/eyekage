package com.eyekage.di

import android.content.Context
import com.eyekage.core.behavior.PersonalityEngine
import com.eyekage.core.ports.FaceObservationSource
import com.eyekage.core.ports.HandGestureSource
import com.eyekage.core.ports.MemoryRepository
import com.eyekage.core.ports.PostureObservationSource
import com.eyekage.core.ports.PreferencesRepository
import com.eyekage.core.ports.VoiceCommandSource
import com.eyekage.platform.audio.SpeechRecognizerVoiceSource
import com.eyekage.platform.preferences.DataStoreMemoryRepository
import com.eyekage.platform.preferences.DataStorePreferencesRepository
import com.eyekage.platform.vision.VisionFaceObservationSource
import com.eyekage.platform.vision.VisionHandGestureSource
import com.eyekage.platform.vision.VisionHeadPoseSource

/**
 * Manual Dependency Injection Container (ADR-007).
 * Pure-Kotlin composition root with zero annotation processors and zero reflection.
 */
interface AppContainer {
    val preferencesRepository: PreferencesRepository
    val faceObservationSource: FaceObservationSource
    val memoryRepository: MemoryRepository
    val personalityEngine: PersonalityEngine
    val voiceCommandSource: VoiceCommandSource
    val postureObservationSource: PostureObservationSource
    val handGestureSource: HandGestureSource
}

class DefaultAppContainer(
    private val appContext: Context
) : AppContainer {

    override val preferencesRepository: PreferencesRepository by lazy {
        DataStorePreferencesRepository(appContext)
    }

    override val postureObservationSource: PostureObservationSource by lazy {
        VisionHeadPoseSource()
    }

    override val handGestureSource: HandGestureSource by lazy {
        VisionHandGestureSource(appContext)
    }

    override val faceObservationSource: FaceObservationSource by lazy {
        VisionFaceObservationSource(appContext).apply {
            headPoseSource = postureObservationSource as? VisionHeadPoseSource
            handGestureSource = this@DefaultAppContainer.handGestureSource as? VisionHandGestureSource
        }
    }

    override val memoryRepository: MemoryRepository by lazy {
        DataStoreMemoryRepository(appContext)
    }

    override val personalityEngine: PersonalityEngine by lazy {
        PersonalityEngine()
    }

    override val voiceCommandSource: VoiceCommandSource by lazy {
        SpeechRecognizerVoiceSource(appContext)
    }
}
