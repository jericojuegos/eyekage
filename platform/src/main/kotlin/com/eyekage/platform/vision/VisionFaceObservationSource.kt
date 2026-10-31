package com.eyekage.platform.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.eyekage.core.model.FaceObservation
import com.eyekage.core.model.FaceObservationTransform
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

/**
 * MediaPipe Tasks Vision implementation of [FaceObservationSource] powered by CameraX.
 *
 * Captures front-facing camera frames headlessly via [CameraXManager], runs inference
 * through the on-device `face_detector.tflite` model with zero network requirements,
 * and emits normalized [FaceObservation] values in centered [-1.0, 1.0] coordinates.
 */
class VisionFaceObservationSource(
    private val context: Context
) : FaceObservationSource {

    private val _observations = MutableStateFlow(FaceObservation.Empty)
    override val observations: StateFlow<FaceObservation> = _observations.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var faceDetector: FaceDetector? = null

    private val cameraXManager: CameraXManager by lazy {
        CameraXManager(context) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val timestampNanos = imageProxy.imageInfo.timestamp
            val bitmap = imageProxy.toBitmap()
            processBitmap(bitmap, rotationDegrees, timestampNanos)
        }
    }

    init {
        initDetector()
    }

    /**
     * Binds the camera analysis pipeline to an Activity or Fragment lifecycle.
     */
    fun bindLifecycle(owner: LifecycleOwner) {
        cameraXManager.bindLifecycle(owner)
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
        } catch (e: Exception) {
            Log.w(TAG, "MediaPipe face detector initialization deferred/failed: ${e.message}")
            faceDetector = null
        }
    }

    /**
     * Process a single bitmap frame synchronously or asynchronously.
     * Normalized coordinates: [-1.0, 1.0] where (0.0, 0.0) is center.
     *
     * @param bitmap The frame image bitmap.
     * @param rotationDegrees Sensor orientation rotation degrees (e.g. 0, 90, 270).
     * @param timestampNanos Frame capture timestamp in nanoseconds.
     */
    fun processBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        timestampNanos: Long = System.nanoTime()
    ) {
        val detector = faceDetector ?: return
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result: FaceDetectorResult = detector.detect(mpImage)

        val detections = result.detections()
        if (!detections.isNullOrEmpty()) {
            val primaryFace = detections[0]
            val box = primaryFace.boundingBox()
            val imgWidth = bitmap.width.toFloat()
            val imgHeight = bitmap.height.toFloat()

            // Calculate raw center coordinates in [0.0, 1.0] image space
            val rawCenterX = box.centerX() / imgWidth
            val rawCenterY = box.centerY() / imgHeight
            val rawWidth = box.width() / imgWidth
            val rawHeight = box.height() / imgHeight

            // Map to centered [-1.0, 1.0] gaze space with front camera mirroring and rotation
            val (normX, normY) = FaceObservationTransform.mapCoordinates(
                rawCenterX = rawCenterX,
                rawCenterY = rawCenterY,
                rotationDegrees = rotationDegrees,
                isFrontCamera = true
            )

            val (normWidth, normHeight) = FaceObservationTransform.mapDimensions(
                rawWidth = rawWidth,
                rawHeight = rawHeight,
                rotationDegrees = rotationDegrees
            )

            val confidence = primaryFace.categories().firstOrNull()?.score() ?: DEFAULT_CONFIDENCE

            _observations.value = FaceObservation(
                normalizedX = normX,
                normalizedY = normY,
                confidence = confidence.coerceIn(0f, 1f),
                boundingBoxWidth = normWidth,
                boundingBoxHeight = normHeight,
                timestampNanos = timestampNanos
            )
        }
    }

    override fun start() {
        if (faceDetector == null) {
            initDetector()
        }
        val isReady = faceDetector != null
        _isRunning.value = isReady
        if (isReady) {
            cameraXManager.start()
        }
    }

    override fun stop() {
        _isRunning.value = false
        cameraXManager.stop()
        faceDetector?.close()
        faceDetector = null
        _observations.value = FaceObservation.Empty
    }
}
