package com.eyekage.platform.vision

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraXManager"
private const val TARGET_WIDTH = 640
private const val TARGET_HEIGHT = 480

/**
 * Headless CameraX controller for EyeKage on-device vision pipeline.
 *
 * Requirements satisfied:
 * - Req 1.1: Front camera with ~480p target resolution and STRATEGY_KEEP_ONLY_LATEST.
 * - Req 1.2: LifecycleOwner binding with automatic release on pause/stop.
 * - Req 1.3: Mandatory imageProxy.close() in finally block to prevent buffer starvation.
 */
class CameraXManager(
    private val context: Context,
    private val onFrameReceived: (ImageProxy) -> Unit
) : DefaultLifecycleObserver {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var isAnalysisStarted = false

    private val analysisExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "eyekage-camerax-analysis").apply {
                isDaemon = true
            }
        }
    }

    /**
     * Binds this manager to a [LifecycleOwner] (e.g. ComponentActivity).
     */
    fun bindLifecycle(owner: LifecycleOwner) {
        if (lifecycleOwner == owner) return
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = owner
        owner.lifecycle.addObserver(this)
        if (isAnalysisStarted) {
            bindCameraUseCases()
        }
    }

    /**
     * Starts the camera capture and analysis pipeline.
     */
    fun start() {
        isAnalysisStarted = true
        bindCameraUseCases()
    }

    /**
     * Stops the camera pipeline and unbinds all CameraX use cases.
     */
    fun stop() {
        isAnalysisStarted = false
        unbindCameraUseCases()
    }

    override fun onPause(owner: LifecycleOwner) {
        unbindCameraUseCases()
    }

    override fun onStop(owner: LifecycleOwner) {
        unbindCameraUseCases()
    }

    override fun onResume(owner: LifecycleOwner) {
        if (isAnalysisStarted) {
            bindCameraUseCases()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null
    }

    private fun bindCameraUseCases() {
        val owner = lifecycleOwner ?: (context as? LifecycleOwner)
        if (owner == null) {
            Log.d(TAG, "LifecycleOwner not yet attached. Camera bind will trigger on attachment.")
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                provider.unbindAll()

                val selector = CameraSelector.DEFAULT_FRONT_CAMERA

                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(TARGET_WIDTH, TARGET_HEIGHT),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val analysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    try {
                        onFrameReceived(imageProxy)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error in frame analysis pipeline", e)
                    } finally {
                        imageProxy.close()
                    }
                }

                imageAnalysis = analysis

                if (isAnalysisStarted) {
                    provider.bindToLifecycle(owner, selector, analysis)
                    Log.d(TAG, "CameraX front-camera ImageAnalysis bound to lifecycle.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind CameraX use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun unbindCameraUseCases() {
        try {
            cameraProvider?.unbindAll()
            imageAnalysis?.clearAnalyzer()
            imageAnalysis = null
        } catch (e: Exception) {
            Log.w(TAG, "Error unbinding CameraX use cases", e)
        }
    }

    fun shutdown() {
        stop()
        try {
            analysisExecutor.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down analysis executor", e)
        }
    }
}
