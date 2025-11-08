package com.gf.mirror

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gf.mirror.core.common.DefaultConfigManager
import com.gf.mirror.core.common.DefaultLocalizationManager
import com.gf.mirror.core.common.DefaultPerformanceMonitor
import com.gf.mirror.core.opengl.DefaultEffectManager
import com.gf.mirror.core.camera.CameraXManager
import com.gf.mirror.core.capture.DefaultImageCaptureManager
import com.gf.mirror.ui.theme.GFMirrorTheme
import com.gf.mirror.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    
    // Core managers
    private lateinit var configManager: com.gf.mirror.core.common.ConfigManager
    private lateinit var localizationManager: com.gf.mirror.core.common.LocalizationManager
    private lateinit var performanceMonitor: com.gf.mirror.core.common.PerformanceMonitor
    private lateinit var effectManager: com.gf.mirror.core.opengl.EffectManager
    private lateinit var cameraManager: com.gf.mirror.core.camera.CameraManager
    private lateinit var imageCaptureManager: com.gf.mirror.core.capture.ImageCaptureManager
    
    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, initialize camera
            initializeCamera()
        } else {
            // Permission denied, show message
            // TODO: Show permission denied message
        }
    }
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(GFMirrorApplication.applyLocale(newBase, GFMirrorApplication.getSavedLanguage(newBase)))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize core managers
        initializeCoreManagers()
        
        // Request camera permission
        requestCameraPermission()
        
        setContent {
            GFMirrorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        configManager = configManager,
                        localizationManager = localizationManager,
                        performanceMonitor = performanceMonitor,
                        effectManager = effectManager,
                        cameraManager = cameraManager,
                        imageCaptureManager = imageCaptureManager
                    )
                }
            }
        }
    }
    
    private fun initializeCoreManagers() {
        configManager = DefaultConfigManager(this)
        localizationManager = DefaultLocalizationManager(this)
        performanceMonitor = DefaultPerformanceMonitor(this)
        effectManager = DefaultEffectManager()
        cameraManager = CameraXManager(this, this)
        imageCaptureManager = DefaultImageCaptureManager(this)
    }
    
    override fun onResume() {
        super.onResume()
        performanceMonitor.startFrameRateMonitoring()
    }
    
    override fun onPause() {
        super.onPause()
        performanceMonitor.stopFrameRateMonitoring()
        cameraManager.stopPreview()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraManager.release()
    }
    
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                initializeCamera()
            }
            else -> {
                // Request permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun initializeCamera() {
        // Camera will be initialized when needed in MainScreen
    }
}