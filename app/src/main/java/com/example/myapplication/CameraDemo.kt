package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.example.myapplication.vision.HandLandmarkerHelper
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewActivity : ComponentActivity() {
    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        val ctx = this
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        backgroundExecutor = Executors.newSingleThreadExecutor()
//        backgroundExecutor.execute {
        backgroundExecutor.execute {
            Looper.prepare()
            handLandmarkerHelper = HandLandmarkerHelper(
                context = ctx,
                runningMode = RunningMode.LIVE_STREAM,
                currentDelegate = HandLandmarkerHelper.DELEGATE_CPU,
                handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
                    override fun onError(error: String, errorCode: Int) {
                        mainLooper.run {
                            Toast.makeText(this@CameraPreviewActivity, error, Toast.LENGTH_SHORT).show()
                        }
                        return;
                    }

                    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                        println("Got results from hand landmarks helper")
                        mainHandler.post {
                            setHandLandmarks(resultBundle)
                        }
                    }

                }
            )
        }

        setContent {
            CameraPreviewWithHandLandmarks(backgroundExecutor)
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundExecutor.shutdown()
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    private lateinit var setHandLandmarks: (HandLandmarkerHelper.ResultBundle?) -> Unit

    @Composable
    fun CameraPreviewWithHandLandmarks(cameraExecutor: ExecutorService) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
//
//    val (handLandmarkerHelper, setHandLandmarksHelper) = remember { mutableStateOf<HandLandmarkerHelper?>(null) }
        val (handLandmarks, setHandLandmarks) = remember { mutableStateOf<HandLandmarkerHelper.ResultBundle?>(null) }
        this.setHandLandmarks = setHandLandmarks
//    LaunchedEffect(Unit) {
//        setHandLandmarksHelper(HandLandmarkerHelper(
//            context = context,
//            runningMode = RunningMode.LIVE_STREAM,
//            handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
//                override fun onError(error: String, errorCode: Int) {
//                    Log.e("HandLandmarker", "Error: $error")
//                }
//
//                override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
//                    setHandLandmarks(resultBundle)
//                }
//            }
//        ))
//    }

        Box(modifier = Modifier.fillMaxSize()) {

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setTargetRotation(previewView.display.rotation)
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(backgroundExecutor) { image ->
                                    handLandmarkerHelper.detectLiveStream(
                                        imageProxy = image,
                                        isFrontCamera = false
                                    )
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                this@CameraPreviewActivity,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Use case binding failed", e)
                        }
                    }, executor)
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )

            HandLandmarksOverlay(handLandmarks)

            Button(onClick = {
                backgroundExecutor.execute {
                    handLandmarkerHelper.clearHandLandmarker()
                    handLandmarkerHelper.setupHandLandmarker()
                }
            }) {
                Text(text = "Reset")
            }
        }
    }

    @Composable
    fun HandLandmarksOverlay(resultBundle: HandLandmarkerHelper.ResultBundle?) {
        Canvas(modifier = Modifier.fillMaxSize()) {

//        drawCircle(Color.Red, radius = 100.0f, center = Offset(100f, 100f))

            resultBundle?.let { bundle ->

                val result = bundle.results.firstOrNull()
                result?.landmarks()?.forEach { landmarks ->
                    landmarks.forEach { landmark ->
                        val x = landmark.x() * size.width
                        val y = landmark.y() * size.height
                        drawCircle(
                            color = Color.Green,
                            radius = 8f,
                            center = Offset(x, y)
                        )
                    }
                }

                // Draw connections between landmarks
                result?.landmarks()?.forEach { landmarks ->
                    HandLandmarker.HAND_CONNECTIONS.forEach { connection ->
                        val start = landmarks[connection.start()]
                        val end = landmarks[connection.end()]

                        drawLine(
                            color = Color.Blue,
                            start = Offset(start.x() * size.width, start.y() * size.height),
                            end = Offset(end.x() * size.width, end.y() * size.height),
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }
    }
}