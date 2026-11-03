package com.eyekage.platform.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.eyekage.core.model.FaceObservationTransform
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandObservation
import com.eyekage.core.ports.HandGestureSource
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "VisionHandGestureSource"
private const val MODEL_ASSET_NAME = "gesture_recognizer.task"
private const val MIN_INFERENCE_INTERVAL_NS = 66_000_000L // ~15 Hz throttling for thermal efficiency

/**
 * On-device implementation of [HandGestureSource] powered by MediaPipe Tasks Vision.
 *
 * Guarantees:
 * - 100% on-device processing. Zero network communication.
 * - Zero frame, crop, or handprint persistence to disk or database.
 * - Throttles inference to 10-15 Hz to preserve thermal equilibrium on Helio G88.
 * - Lifecycle-aware shutdown and clean resource release.
 */
class VisionHandGestureSource(
    private val context: Context? = null
) : HandGestureSource {

    private val _handGestures = MutableStateFlow(HandObservation.Empty)
    override val handGestures: StateFlow<HandObservation> = _handGestures.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var gestureRecognizer: GestureRecognizer? = null
    private var lastInferenceTimestampNs: Long = 0L

    init {
        context?.let { initRecognizer(it) }
    }

    private fun initRecognizer(ctx: Context) {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_NAME)
                .build()

            val options = GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setMinHandDetectionConfidence(0.5f)
                .build()

            gestureRecognizer = GestureRecognizer.createFromOptions(ctx, options)
        } catch (e: Exception) {
            Log.w(TAG, "MediaPipe GestureRecognizer initialization deferred/failed: ${e.message}")
            gestureRecognizer = null
        }
    }

    /**
     * Process an incoming frame bitmap from CameraX.
     */
    fun processFrame(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        timestampNanos: Long = System.nanoTime()
    ) {
        if (!_isTracking.value) return

        // Throttle inference rate to ~15 Hz (Req 2.2)
        if (timestampNanos - lastInferenceTimestampNs < MIN_INFERENCE_INTERVAL_NS) {
            return
        }
        lastInferenceTimestampNs = timestampNanos

        val recognizer = gestureRecognizer ?: return

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result: GestureRecognizerResult = recognizer.recognize(mpImage)
            processResult(result, bitmap.width, bitmap.height, rotationDegrees, timestampNanos)
        } catch (e: Exception) {
            Log.w(TAG, "Gesture recognition error: ${e.message}")
        }
    }

    /**
     * Maps raw recognizer outputs into a centered normalized [HandObservation].
     */
    fun processResult(
        result: GestureRecognizerResult,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int = 0,
        timestampNanos: Long = System.nanoTime()
    ) {
        val gestures = result.gestures()
        if (gestures.isNullOrEmpty() || gestures[0].isEmpty()) {
            _handGestures.value = HandObservation.Empty.copy(timestampNanos = timestampNanos)
            return
        }

        val primaryCategory = gestures[0][0]
        val categoryName = primaryCategory.categoryName()
        val score = primaryCategory.score()

        val gesture = mapCategoryToHandGesture(categoryName)

        // Extract normalized coordinates from landmarks if available
        val landmarksList = result.landmarks()
        val (normX, normY, boundingArea) = if (!landmarksList.isNullOrEmpty() && landmarksList[0].isNotEmpty()) {
            val marks = landmarksList[0]
            var minX = 1.0f
            var maxX = 0.0f
            var minY = 1.0f
            var maxY = 0.0f
            for (lm in marks) {
                if (lm.x() < minX) minX = lm.x()
                if (lm.x() > maxX) maxX = lm.x()
                if (lm.y() < minY) minY = lm.y()
                if (lm.y() > maxY) maxY = lm.y()
            }
            val rawCenterX = (minX + maxX) * 0.5f
            val rawCenterY = (minY + maxY) * 0.5f
            val (mappedX, mappedY) = FaceObservationTransform.mapCoordinates(
                rawCenterX = rawCenterX,
                rawCenterY = rawCenterY,
                rotationDegrees = rotationDegrees,
                isFrontCamera = true
            )
            val area = ((maxX - minX) * (maxY - minY)).coerceIn(0f, 1f)
            Triple(mappedX, mappedY, area)
        } else {
            Triple(0f, 0f, 0.1f)
        }

        _handGestures.value = HandObservation(
            gesture = gesture,
            confidence = score.coerceIn(0f, 1f),
            normalizedCenterX = normX,
            normalizedCenterY = normY,
            boundingBoxArea = boundingArea,
            timestampNanos = timestampNanos
        )
    }

    /**
     * Process an observation directly (used by tests or alternate detectors).
     */
    fun emitObservation(
        gesture: HandGesture,
        confidence: Float,
        rawCenterX: Float = 0.5f,
        rawCenterY: Float = 0.5f,
        rotationDegrees: Int = 0,
        boundingBoxArea: Float = 0.15f,
        timestampNanos: Long = System.nanoTime()
    ) {
        val (normX, normY) = FaceObservationTransform.mapCoordinates(
            rawCenterX = rawCenterX,
            rawCenterY = rawCenterY,
            rotationDegrees = rotationDegrees,
            isFrontCamera = true
        )

        _handGestures.value = HandObservation(
            gesture = gesture,
            confidence = confidence.coerceIn(0f, 1f),
            normalizedCenterX = normX,
            normalizedCenterY = normY,
            boundingBoxArea = boundingBoxArea,
            timestampNanos = timestampNanos
        )
    }

    override fun start() {
        _isTracking.value = true
        if (gestureRecognizer == null && context != null) {
            initRecognizer(context)
        }
    }

    override fun stop() {
        _isTracking.value = false
        _handGestures.value = HandObservation.Empty
        gestureRecognizer?.close()
        gestureRecognizer = null
    }

    companion object {
        fun mapCategoryToHandGesture(category: String): HandGesture = when (category.lowercase()) {
            "open_palm" -> HandGesture.WAVE
            "victory" -> HandGesture.PEACE_SIGN
            "thumb_up" -> HandGesture.THUMBS_UP
            "closed_fist" -> HandGesture.CLOSED_FIST
            "stop", "flat_palm" -> HandGesture.FLAT_PALM_STOP
            else -> HandGesture.NONE
        }
    }
}
