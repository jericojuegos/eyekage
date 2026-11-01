package com.eyekage.platform.vision

import com.eyekage.core.model.FacialLandmarks
import com.eyekage.core.model.HeadPose
import com.eyekage.core.model.HeadPoseTransform
import com.eyekage.core.model.PostureObservation
import com.eyekage.core.model.PostureState
import com.eyekage.core.model.ProximityZone
import com.eyekage.core.motion.OneEuroFilter
import com.eyekage.core.ports.PostureObservationSource
import com.google.mediapipe.tasks.components.containers.Detection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * On-device implementation of [PostureObservationSource] backed by MediaPipe face landmarks
 * and filtered with 4-channel [OneEuroFilter] for smooth, jitter-free 3D orientation.
 *
 * Guarantees:
 * - 100% on-device processing. Zero network communication.
 * - Zero frame, crop, or biometric persistence to disk or database.
 * - Lifecycle-aware shutdown and filter reset.
 */
class VisionHeadPoseSource : PostureObservationSource {

    private val _posture = MutableStateFlow(PostureObservation.Neutral)
    override val posture: StateFlow<PostureObservation> = _posture.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val pitchFilter = OneEuroFilter(minCutoff = 1.0f, beta = 0.007f)
    private val yawFilter = OneEuroFilter(minCutoff = 1.0f, beta = 0.007f)
    private val rollFilter = OneEuroFilter(minCutoff = 1.0f, beta = 0.007f)
    private val distanceFilter = OneEuroFilter(minCutoff = 1.0f, beta = 0.007f)

    /**
     * Process landmarks from a MediaPipe [Detection].
     */
    fun processDetection(
        detection: Detection,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int = 0,
        timestampNanos: Long = System.nanoTime()
    ) {
        val keypointsOpt = detection.keypoints()
        val landmarks: FacialLandmarks = if (keypointsOpt.isPresent && keypointsOpt.get().size >= 4) {
            val kps = keypointsOpt.get()
            // In MediaPipe Tasks BlazeFace:
            // kps[0] = right eye (face's right / image left)
            // kps[1] = left eye (face's left / image right)
            // kps[2] = nose tip
            // kps[3] = mouth center
            // kps[4] = right ear tragion (optional)
            // kps[5] = left ear tragion (optional)
            FacialLandmarks(
                leftEye = Pair(kps[1].x(), kps[1].y()),
                rightEye = Pair(kps[0].x(), kps[0].y()),
                noseTip = Pair(kps[2].x(), kps[2].y()),
                mouthCenter = Pair(kps[3].x(), kps[3].y()),
                leftEar = if (kps.size > 5) Pair(kps[5].x(), kps[5].y()) else null,
                rightEar = if (kps.size > 4) Pair(kps[4].x(), kps[4].y()) else null
            )
        } else {
            // Geometric fallback from face bounding box
            val box = detection.boundingBox()
            val w = imageWidth.toFloat()
            val h = imageHeight.toFloat()
            val rawCenterX = box.centerX() / w
            val rawCenterY = box.centerY() / h
            val rawW = box.width() / w
            val rawH = box.height() / h
            FacialLandmarks(
                leftEye = Pair(rawCenterX - rawW * 0.2f, rawCenterY - rawH * 0.15f),
                rightEye = Pair(rawCenterX + rawW * 0.2f, rawCenterY - rawH * 0.15f),
                noseTip = Pair(rawCenterX, rawCenterY),
                mouthCenter = Pair(rawCenterX, rawCenterY + rawH * 0.25f)
            )
        }

        processLandmarks(
            landmarks = landmarks,
            rotationDegrees = rotationDegrees,
            isFrontCamera = true,
            timestampNanos = timestampNanos
        )
    }

    /**
     * Process landmarks directly, applying 4-channel One-Euro smoothing.
     */
    fun processLandmarks(
        landmarks: FacialLandmarks,
        rotationDegrees: Int = 0,
        isFrontCamera: Boolean = true,
        timestampNanos: Long = System.nanoTime()
    ) {
        val rawObs = HeadPoseTransform.calculatePosture(
            landmarks = landmarks,
            rotationDegrees = rotationDegrees,
            isFrontCamera = isFrontCamera,
            timestampNanos = timestampNanos
        )

        val timestampSeconds = timestampNanos / 1_000_000_000.0

        val smoothedPitch = pitchFilter.filter(rawObs.headPose.pitchDegrees, timestampSeconds)
        val smoothedYaw = yawFilter.filter(rawObs.headPose.yawDegrees, timestampSeconds)
        val smoothedRoll = rollFilter.filter(rawObs.headPose.rollDegrees, timestampSeconds)
        val smoothedDistance = distanceFilter.filter(rawObs.estimatedDistanceRatio, timestampSeconds)

        val smoothedHeadPose = HeadPose(
            pitchDegrees = smoothedPitch,
            yawDegrees = smoothedYaw,
            rollDegrees = smoothedRoll,
            timestampNanos = timestampNanos
        )

        val proximity = when {
            smoothedDistance >= 0.35f -> ProximityZone.TOO_CLOSE
            smoothedDistance >= 0.22f -> ProximityZone.NEAR
            smoothedDistance >= 0.12f -> ProximityZone.NORMAL
            else -> ProximityZone.FAR
        }

        val postureState = when {
            abs(smoothedYaw) >= HeadPoseTransform.YAW_TURNED_AWAY_THRESHOLD_DEGREES -> PostureState.TURNED_AWAY
            proximity == ProximityZone.TOO_CLOSE -> PostureState.LEANING_CLOSE
            smoothedPitch <= HeadPoseTransform.PITCH_SLOUCH_THRESHOLD_DEGREES && proximity != ProximityZone.FAR -> PostureState.SLOUCHING_DOWN
            abs(smoothedRoll) >= HeadPoseTransform.ROLL_TILT_THRESHOLD_DEGREES -> PostureState.TILTED_CURIOUS
            else -> PostureState.BALANCED
        }

        _posture.value = PostureObservation(
            headPose = smoothedHeadPose,
            proximity = proximity,
            estimatedDistanceRatio = smoothedDistance,
            postureState = postureState,
            timestampNanos = timestampNanos
        )
    }

    fun onFaceLost() {
        _posture.value = PostureObservation.Neutral
        pitchFilter.reset()
        yawFilter.reset()
        rollFilter.reset()
        distanceFilter.reset()
    }

    override fun start() {
        _isTracking.value = true
    }

    override fun stop() {
        _isTracking.value = false
        onFaceLost()
    }
}
