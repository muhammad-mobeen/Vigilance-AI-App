package com.example.vigilanceai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.vigilanceai.databinding.ActivityMainBinding
import com.example.vigilanceai.drowsinessdetection.DrowsinessDetectionActivity

class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            startScanner()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openCamera.setOnClickListener {
            requestCameraANdStartScanner()
        }

        binding.drowsinessDetectionBtn.setOnClickListener {
            DrowsinessDetectionActivity.start(this)
        }
    }

    private fun requestCameraANdStartScanner() {
        if(isPermissionGranted(cameraPermission)) {
            Log.println(Log.ASSERT,"TAG","Msg")
            startScanner()
        }else{
            requestCameraPermission()
        }
    }

    private fun startScanner(){
        ScannerActivity.startScanner(this) {

        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }
}