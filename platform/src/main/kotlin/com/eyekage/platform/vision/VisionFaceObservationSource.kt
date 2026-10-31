package com.eyekage.platform.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.ports.FaceObservationSource
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "VisionFaceObservation"
private const val MODEL_ASSET_NAME = "face_detector.tflite"
private const val MIN_CONFIDENCE_THRESHOLD = 0.5f
private const val DEFAULT_CONFIDENCE = 0.5f
private const val COORDINATE_CENTER_OFFSET = 0.5f
private const val COORDINATE_SCALE_FACTOR = 2.0f

/**
 * MediaPipe Tasks Vision implementation of [FaceObservationSource].
 * Uses bundled on-device `face_detector.tflite` model with zero network requirements.
 */
class VisionFaceObservationSource(
    private val context: Context
) : FaceObservationSource {

    private val _observations = MutableStateFlow(FaceObservation.Empty)
    override val observations: StateFlow<FaceObservation> = _observations.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var faceDetector: FaceDetector? = null

    init {
        initDetector()
    }

    private fun initDetector() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_NAME)
                .build()

            val options = FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinDetectionConfidence(MIN_CONFIDENCE_THRESHOLD)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            faceDetector = FaceDetector.createFromOptions(context, options)
        } catch (e: com.google.mediapipe.framework.MediaPipeException) {
            Log.w(TAG, "MediaPipe face detector initialization deferred/failed", e)
            faceDetector = null
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "MediaPipe face detector arguments invalid", e)
            faceDetector = null
        }
    }

    /**
     * Process a single bitmap frame synchronously or asynchronously.
     * Normalized coordinates: [-1.0, 1.0] where 0.0 is center.
     */
    fun processBitmap(bitmap: Bitmap, timestampNanos: Long = System.nanoTime()) {
        val detector = faceDetector ?: return
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result: FaceDetectorResult = detector.detect(mpImage)

        val detections = result.detections()
        if (!detections.isNullOrEmpty()) {
            val primaryFace = detections[0]
            val box = primaryFace.boundingBox()
            val imgWidth = bitmap.width.toFloat()
            val imgHeight = bitmap.height.toFloat()

            // Calculate center of face bounding box in normalized [-1.0, 1.0] coordinates
            val centerX = box.centerX() / imgWidth
            val centerY = box.centerY() / imgHeight

            val normX = ((centerX - COORDINATE_CENTER_OFFSET) * COORDINATE_SCALE_FACTOR).coerceIn(-1f, 1f)
            val normY = ((centerY - COORDINATE_CENTER_OFFSET) * COORDINATE_SCALE_FACTOR).coerceIn(-1f, 1f)

            val confidence = primaryFace.categories().firstOrNull()?.score() ?: DEFAULT_CONFIDENCE

            _observations.value = FaceObservation(
                normalizedX = normX,
                normalizedY = normY,
                confidence = confidence.coerceIn(0f, 1f),
                timestampNanos = timestampNanos
            )
        }
    }

    override fun start() {
        if (faceDetector == null) {
            initDetector()
        }
        _isRunning.value = faceDetector != null
    }

    override fun stop() {
        _isRunning.value = false
        faceDetector?.close()
        faceDetector = null
    }
}
