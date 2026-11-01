package com.eyekage.core.model

import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Geometric representation of facial landmarks in normalized image space [0.0, 1.0].
 *
 * For front cameras:
 * - [leftEye]: Person's left eye (from user's perspective; image right before mirroring).
 * - [rightEye]: Person's right eye (from user's perspective; image left before mirroring).
 * - [noseTip]: Tip of the nose.
 * - [mouthCenter]: Center of the lips.
 * - [leftEar]: Person's left ear tragion (optional).
 * - [rightEar]: Person's right ear tragion (optional).
 */
data class FacialLandmarks(
    val leftEye: Pair<Float, Float>,
    val rightEye: Pair<Float, Float>,
    val noseTip: Pair<Float, Float>,
    val mouthCenter: Pair<Float, Float>,
    val leftEar: Pair<Float, Float>? = null,
    val rightEar: Pair<Float, Float>? = null
)

/**
 * Pure-Kotlin geometric triangulation engine for estimating 3D Head Pose Euler angles
 * (Pitch, Yaw, Roll) and spatial proximity from 2D facial landmarks.
 *
 * Zero Android SDK dependencies.
 */
object HeadPoseTransform {

    private const val RAD_TO_DEG = 180.0f / Math.PI.toFloat()
    private const val BASELINE_VERTICAL_RATIO = 0.58f
    private const val PITCH_SCALE_FACTOR = 140.0f
    private const val YAW_SCALE_FACTOR = 90.0f

    // Proximity thresholds based on normalized inter-ocular distance
    private const val PROXIMITY_TOO_CLOSE_THRESHOLD = 0.35f
    private const val PROXIMITY_NEAR_THRESHOLD = 0.22f
    private const val PROXIMITY_NORMAL_THRESHOLD = 0.12f

    // Posture classification thresholds
    const val ROLL_TILT_THRESHOLD_DEGREES = 12.0f
    const val YAW_TURNED_AWAY_THRESHOLD_DEGREES = 25.0f
    const val PITCH_SLOUCH_THRESHOLD_DEGREES = -15.0f

    /**
     * Transform raw landmark point [point] in [0.0, 1.0] image space by [rotationDegrees]
     * and horizontal front-camera mirroring.
     */
    fun transformPoint(
        point: Pair<Float, Float>,
        rotationDegrees: Int = 0,
        isFrontCamera: Boolean = true
    ): Pair<Float, Float> {
        val (rawX, rawY) = point
        val normalizedRotation = ((rotationDegrees % 360) + 360) % 360

        // Step 1: Rotate in [0.0, 1.0] space
        val (rotatedX, rotatedY) = when (normalizedRotation) {
            90 -> Pair(1.0f - rawY, rawX)
            180 -> Pair(1.0f - rawX, 1.0f - rawY)
            270 -> Pair(rawY, 1.0f - rawX)
            else -> Pair(rawX, rawY)
        }

        // Step 2: Mirror horizontally for front camera (so user's right is on user's right)
        val finalX = if (isFrontCamera) 1.0f - rotatedX else rotatedX
        return Pair(finalX.coerceIn(0f, 1f), rotatedY.coerceIn(0f, 1f))
    }

    /**
     * Compute [PostureObservation] from transformed landmarks.
     */
    fun calculatePosture(
        landmarks: FacialLandmarks,
        rotationDegrees: Int = 0,
        isFrontCamera: Boolean = true,
        timestampNanos: Long = System.nanoTime()
    ): PostureObservation {
        // Transform all keypoints to upright, mirrored user space
        val leftEye = transformPoint(landmarks.leftEye, rotationDegrees, isFrontCamera)
        val rightEye = transformPoint(landmarks.rightEye, rotationDegrees, isFrontCamera)
        val nose = transformPoint(landmarks.noseTip, rotationDegrees, isFrontCamera)
        val mouth = transformPoint(landmarks.mouthCenter, rotationDegrees, isFrontCamera)

        val deltaX = leftEye.first - rightEye.first
        val deltaY = leftEye.second - rightEye.second

        val eyeSpan = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
        if (eyeSpan < 0.005f) {
            return PostureObservation.Neutral.copy(timestampNanos = timestampNanos)
        }

        // 1. Roll: lateral tilt angle in degrees.
        // Positive roll = tilting head to user's right (right eye drops, deltaY < 0).
        val rollDegrees = (-atan2(deltaY.toDouble(), deltaX.toDouble()).toFloat() * RAD_TO_DEG)
            .coerceIn(-90f, 90f)

        // 2. Yaw: horizontal turn angle in degrees.
        // Midpoint between eyes in user space
        val midX = (leftEye.first + rightEye.first) * 0.5f
        val interOcularDist = max(deltaX, 0.01f)
        // If user turns right, nose shifts toward right side of face (lower X in user space)
        val yawRatio = (midX - nose.first) / interOcularDist
        val yawDegrees = (yawRatio * YAW_SCALE_FACTOR).coerceIn(-90f, 90f)

        // 3. Pitch: vertical tilt / nod in degrees.
        // Eye-line Y in user space
        val eyeLineY = (leftEye.second + rightEye.second) * 0.5f
        val noseDrop = max(nose.second - eyeLineY, 0.001f)
        val mouthDrop = max(mouth.second - nose.second, 0.001f)
        val verticalRatio = noseDrop / max(noseDrop + mouthDrop, 0.002f)
        // Looking down (slouch) increases verticalRatio -> negative pitchDegrees
        val pitchDegrees = ((BASELINE_VERTICAL_RATIO - verticalRatio) * PITCH_SCALE_FACTOR)
            .coerceIn(-90f, 90f)

        val headPose = HeadPose(
            pitchDegrees = pitchDegrees,
            yawDegrees = yawDegrees,
            rollDegrees = rollDegrees,
            timestampNanos = timestampNanos
        )

        // 4. Proximity classification
        val proximity = when {
            eyeSpan >= PROXIMITY_TOO_CLOSE_THRESHOLD -> ProximityZone.TOO_CLOSE
            eyeSpan >= PROXIMITY_NEAR_THRESHOLD -> ProximityZone.NEAR
            eyeSpan >= PROXIMITY_NORMAL_THRESHOLD -> ProximityZone.NORMAL
            else -> ProximityZone.FAR
        }

        // 5. Posture classification
        val postureState = when {
            kotlin.math.abs(yawDegrees) >= YAW_TURNED_AWAY_THRESHOLD_DEGREES -> PostureState.TURNED_AWAY
            proximity == ProximityZone.TOO_CLOSE -> PostureState.LEANING_CLOSE
            pitchDegrees <= PITCH_SLOUCH_THRESHOLD_DEGREES && proximity != ProximityZone.FAR -> PostureState.SLOUCHING_DOWN
            kotlin.math.abs(rollDegrees) >= ROLL_TILT_THRESHOLD_DEGREES -> PostureState.TILTED_CURIOUS
            else -> PostureState.BALANCED
        }

        return PostureObservation(
            headPose = headPose,
            proximity = proximity,
            estimatedDistanceRatio = eyeSpan,
            postureState = postureState,
            timestampNanos = timestampNanos
        )
    }
}
