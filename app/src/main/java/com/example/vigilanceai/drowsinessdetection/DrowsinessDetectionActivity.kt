package com.example.vigilanceai.drowsinessdetection

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.vigilanceai.CameraXViewModel
import com.example.vigilanceai.databinding.ActivityDrowsinessDetectionBinding
import com.example.vigilanceai.databinding.ActivityFaceDetectionBinding
import com.example.vigilanceai.facedetection.FaceBox
import com.example.vigilanceai.facedetection.FaceBoxOverlay
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class DrowsinessDetectionActivity : AppCompatActivity() {

    private val DROWSINESS_THRESHOLD = 0.5f
    private val FRAMES_THRESHOLD = 3
    private var frames_counter = 0
    private var redundant = 0

    private lateinit var binding: ActivityDrowsinessDetectionBinding

    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrowsinessDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException"
            )
        }
    }

    private fun bindInputAnalyser() {

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
//                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation (binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor ()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(detector, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        detector: FaceDetector,
        imageProxy: ImageProxy
    ) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                binding.faceBoxOverlay.clear()
                faces.forEach { face ->
                    val box = FaceBox(binding.faceBoxOverlay, face, imageProxy.cropRect)
                    binding.faceBoxOverlay.add(box)
                    drowsyAlert(face.leftEyeOpenProbability, face.rightEyeOpenProbability)
                    Log.println(Log.ASSERT,"Left EYE",face.leftEyeOpenProbability.toString())
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.message ?: it.toString())
            }.addOnCompleteListener {
                imageProxy.close()
            }
    }

//    fun invokeAlert() {
//        ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_PIP,150)
//        val totalTime = measureTimeMillis {
//            val endTime = System.currentTimeMillis() + 5000 // 5000 milliseconds = 5 seconds
//
//            while (System.currentTimeMillis() < endTime) {
//                // Your loop logic here
//                var timeremaining = System.currentTimeMillis() - endTime
//                Log.println(Log.ASSERT, "Waiting", "$timeremaining")
//            }
//        }
//        binding.drowsyAlertText.visibility = View.INVISIBLE
//        Log.println(Log.ASSERT, "\"Loop ran for ${totalTime / 1000} seconds.\"", "$FRAMES_THRESHOLD")
//    }

    fun invokeAlert() {
        Toast.makeText(applicationContext, "You are Feeling Tired! Please Take a Rest!", Toast.LENGTH_LONG).show()
        ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP,10000)
        ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP,10000)
//        val job = GlobalScope.launch {
//            val totalTime = measureTimeMillis {
//                val endTime = System.currentTimeMillis() + 5000 // 5000 milliseconds = 5 seconds
//
//                while (System.currentTimeMillis() < endTime) {
//                    // Your loop logic here
//                    var timeremaining = endTime - System.currentTimeMillis()
//                    Log.println(Log.ASSERT, "Waiting", "$timeremaining")
//                    delay(100) // Delay for a short interval (e.g., 100 milliseconds)
//                }
//            }
//
//            binding.drowsyAlertText.visibility = View.INVISIBLE
//            Log.println(Log.ASSERT, "\"Loop ran for ${totalTime / 1000} seconds.\"", "$FRAMES_THRESHOLD")
//        }

        // Wait for the job to complete
//        runBlocking {
//            job.join()
//        }
    }

    fun drowsyAlert(
        leftEye: Float?,
        rightEye: Float?
    ) : Boolean {
        if (redundant > 0) {
            redundant--
            Log.println(Log.ASSERT, "Redundant", "$redundant")
        } else {
            binding.drowsyAlertText.visibility = View.INVISIBLE
        }
        if (leftEye == null && rightEye == null) {
            frames_counter = 0
            return false
        }else if (leftEye != null && rightEye != null) {
            if (leftEye < DROWSINESS_THRESHOLD && rightEye < DROWSINESS_THRESHOLD) {
                frames_counter++
                if (frames_counter >= FRAMES_THRESHOLD) {
                    binding.drowsyAlertText.visibility = View.VISIBLE
                    Log.println(Log.ASSERT, "DrowsyAlert", "$FRAMES_THRESHOLD")
                    invokeAlert()
                    frames_counter = 0
                    redundant = 20
                    return true
                } else {
                    return false
                }
            } else {
                frames_counter = 0
                return false
            }
        } else {
            return false
        }
    }

    companion object {
        private val TAG = DrowsinessDetectionActivity::class.simpleName
        fun start(context: Context) {
            Intent(context, DrowsinessDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }

}