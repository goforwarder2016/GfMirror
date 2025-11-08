package com.gf.mirror.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.gf.mirror.core.capture.ImageCaptureManager
import com.gf.mirror.core.common.LocalizationManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Simplified camera preview component
 * Converts camera feed to Bitmap and passes to OpenGL
 */
@Composable
fun SimpleCameraPreview(
    modifier: Modifier = Modifier,
    localizationManager: LocalizationManager,
    imageCaptureManager: ImageCaptureManager? = null,
    onBitmapUpdate: (Bitmap?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    LaunchedEffect(Unit) {
        try {
            println("SimpleCameraPreview: Getting camera provider...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            println("SimpleCameraPreview: Camera provider obtained successfully")
        } catch (e: Exception) {
            println("SimpleCameraPreview: Failed to get camera provider: ${e.message}")
        }
    }
    
    LaunchedEffect(cameraProvider) {
        cameraProvider?.let { provider ->
            try {
                println("SimpleCameraPreview: Binding camera to lifecycle...")
                
                // Create ImageAnalysis for getting camera frames
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        // Convert ImageProxy to Bitmap
                        val bitmap = imageProxyToBitmap(imageProxy)
                        if (bitmap != null) {
                            onBitmapUpdate(bitmap)
                        }
                    } catch (e: Exception) {
                        Log.e("SimpleCameraPreview", "Error processing image", e)
                    } finally {
                        imageProxy.close()
                    }
                }
                
                // Only bind ImageAnalysis, do not bind Preview and ImageCapture
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                    imageAnalysis
                )
                println("SimpleCameraPreview: Camera bound successfully")
            } catch (e: Exception) {
                println("SimpleCameraPreview: Camera binding failed: ${e.message}")
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

/**
 * Convert ImageProxy to Bitmap
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val buffer = imageProxy.planes[0].buffer
        val pixelStride = imageProxy.planes[0].pixelStride
        val rowStride = imageProxy.planes[0].rowStride
        val rowPadding = rowStride - pixelStride * imageProxy.width
        
        val bitmap = Bitmap.createBitmap(
            imageProxy.width + rowPadding / pixelStride,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        // If image has padding, need to crop
        if (rowPadding > 0) {
            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                imageProxy.width,
                imageProxy.height
            )
            bitmap.recycle()
            croppedBitmap
        } else {
            bitmap
        }
    } catch (e: Exception) {
        Log.e("SimpleCameraPreview", "Error converting ImageProxy to Bitmap", e)
        null
    }
}

/**
 * Small window camera preview (for displaying original feed)
 */
@Composable
fun SmallCameraPreview(
    modifier: Modifier = Modifier,
    localizationManager: LocalizationManager,
    imageCaptureManager: ImageCaptureManager? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    LaunchedEffect(Unit) {
        try {
            println("SmallCameraPreview: Getting camera provider...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            println("SmallCameraPreview: Camera provider obtained successfully")
        } catch (e: Exception) {
            println("SmallCameraPreview: Failed to get camera provider: ${e.message}")
        }
    }
    
    LaunchedEffect(cameraProvider) {
        cameraProvider?.let { provider ->
            try {
                println("SmallCameraPreview: Binding camera to lifecycle...")
                
                val preview = Preview.Builder()
                    .setTargetResolution(android.util.Size(320, 240))
                    .build()
                
                val previewView = androidx.camera.view.PreviewView(context).apply {
                    scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                }
                
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                // Create ImageCapture
                val imageCapture = ImageCapture.Builder()
                    .setTargetResolution(android.util.Size(1280, 720))
                    .build()
                
                // Set ImageCapture to manager
                imageCaptureManager?.setImageCapture(imageCapture)
                
                // Note: do not call unbindAll() here, as SimpleCameraPreview may have already bound ImageAnalysis
                provider.bindToLifecycle(
                    lifecycleOwner,
                    androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                println("SmallCameraPreview: Camera bound successfully")
            } catch (e: Exception) {
                println("SmallCameraPreview: Camera binding failed: ${e.message}")
            }
        }
    }
    
    Box(
        modifier = modifier
            .size(100.dp)
            .alpha(0.9f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                androidx.camera.view.PreviewView(context).apply {
                    scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
