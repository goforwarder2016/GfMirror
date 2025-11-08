package com.gf.mirror.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Unified camera preview component
 * Provides both Preview display and ImageAnalysis data
 */
@Composable
fun UnifiedCameraPreview(
    modifier: Modifier = Modifier,
    localizationManager: LocalizationManager,
    imageCaptureManager: ImageCaptureManager? = null,
    onBitmapUpdate: (Bitmap?) -> Unit,
    showSmallPreview: Boolean = false,
    isFrontCamera: Boolean = false
) {
    println("UnifiedCameraPreview: Component created")
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isCameraBound by remember { mutableStateOf(false) }
    var lastCameraType by remember { mutableStateOf(isFrontCamera) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { 
        androidx.camera.view.PreviewView(context).apply {
            scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
        }
    }
    
    LaunchedEffect(Unit) {
        try {
            println("UnifiedCameraPreview: Getting camera provider...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            println("UnifiedCameraPreview: Camera provider obtained successfully")
        } catch (e: Exception) {
            println("UnifiedCameraPreview: Failed to get camera provider: ${e.message}")
        }
    }
    
    LaunchedEffect(cameraProvider, isFrontCamera) {
        cameraProvider?.let { provider ->
            try {
                // Only rebind if camera type changed or not yet bound
                val cameraTypeChanged = lastCameraType != isFrontCamera
                if (isCameraBound && cameraTypeChanged) {
                    provider.unbindAll()
                    println("UnifiedCameraPreview: Unbound existing camera use cases due to camera type change")
                    isCameraBound = false
                }
                
                if (!isCameraBound) {
                    println("UnifiedCameraPreview: Binding camera to lifecycle... (Front: $isFrontCamera)")
                    isCameraBound = true
                    lastCameraType = isFrontCamera
                    
                    // Create Preview - only set surface provider if showing preview
                    val preview = Preview.Builder()
                        .setTargetResolution(android.util.Size(720, 720))
                        .build()
                    
                    if (showSmallPreview) {
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Create ImageAnalysis for getting camera frames - use same resolution
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(720, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    
                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            println("UnifiedCameraPreview: ImageAnalysis received frame: ${imageProxy.width}x${imageProxy.height}, format: ${imageProxy.format}")
                            // Convert ImageProxy to Bitmap
                            val bitmap = imageProxyToBitmap(imageProxy)
                            if (bitmap != null) {
                                println("UnifiedCameraPreview: Bitmap created successfully: ${bitmap.width}x${bitmap.height}")
                                onBitmapUpdate(bitmap)
                            } else {
                                println("UnifiedCameraPreview: Failed to create bitmap from ImageProxy")
                                onBitmapUpdate(null)
                            }
                        } catch (e: Exception) {
                            Log.e("UnifiedCameraPreview", "Error processing image", e)
                            onBitmapUpdate(null)
                        } finally {
                            imageProxy.close()
                        }
                    }
                    
                    // Create ImageCapture
                    val imageCapture = ImageCapture.Builder()
                        .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                    
                    // Set ImageCapture to manager
                    imageCaptureManager?.setImageCapture(imageCapture)
                    
                    // Select camera
                    val cameraSelector = if (isFrontCamera) {
                        androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                        imageCapture
                    )
                    println("UnifiedCameraPreview: Camera bound successfully")
                }
            } catch (e: Exception) {
                println("UnifiedCameraPreview: Camera binding failed: ${e.message}")
                isCameraBound = false
            }
        }
    }
    
    // Only render AndroidView when showSmallPreview is true
    if (showSmallPreview) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    previewView
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
                    .size(width = 70.dp, height = 100.dp)
                    .alpha(0.7f)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(4.dp))
            )
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
                println("UnifiedCameraPreview: Camera unbound on dispose")
            } catch (e: Exception) {
                println("UnifiedCameraPreview: Error unbinding camera on dispose: ${e.message}")
            }
            cameraExecutor.shutdown()
        }
    }
}

/**
 * Convert ImageProxy to Bitmap
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        // Check image format
        if (imageProxy.format != android.graphics.ImageFormat.YUV_420_888) {
            Log.w("UnifiedCameraPreview", "Unsupported image format: ${imageProxy.format}")
            return null
        }
        
        // Get all components
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        // Debug info: print YUV plane information
        Log.d("UnifiedCameraPreview", "Image size: ${imageProxy.width}x${imageProxy.height}")
        Log.d("UnifiedCameraPreview", "Y plane: size=$ySize, rowStride=${imageProxy.planes[0].rowStride}, pixelStride=${imageProxy.planes[0].pixelStride}")
        Log.d("UnifiedCameraPreview", "U plane: size=$uSize, rowStride=${imageProxy.planes[1].rowStride}, pixelStride=${imageProxy.planes[1].pixelStride}")
        Log.d("UnifiedCameraPreview", "V plane: size=$vSize, rowStride=${imageProxy.planes[2].rowStride}, pixelStride=${imageProxy.planes[2].pixelStride}")
        
        val yImage = ByteArray(ySize)
        val uImage = ByteArray(uSize)
        val vImage = ByteArray(vSize)
        
        yBuffer.get(yImage)
        uBuffer.get(uImage)
        vBuffer.get(vImage)
        
        // Create Bitmap
        val bitmap = Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
        
        // Use Android built-in YUV to RGB conversion
        val pixels = IntArray(imageProxy.width * imageProxy.height)
        
        // Correct YUV_420_888 to RGB conversion
        val yPlane = imageProxy.planes[0]
        val uPlane = imageProxy.planes[1]
        val vPlane = imageProxy.planes[2]
        
        for (y in 0 until imageProxy.height) {
            for (x in 0 until imageProxy.width) {
                val pixelIndex = y * imageProxy.width + x
                
                if (pixelIndex < pixels.size) {
                    // Y component
                    val yIndex = y * yPlane.rowStride + x * yPlane.pixelStride
                    val yValue = if (yIndex < yImage.size) yImage[yIndex].toInt() and 0xFF else 0
                    
                    // UV components: 4:2:0 format, UV planes are independent
                    val uvY = y / 2
                    val uvX = x / 2
                    val uIndex = uvY * uPlane.rowStride + uvX * uPlane.pixelStride
                    val vIndex = uvY * vPlane.rowStride + uvX * vPlane.pixelStride
                    
                    val uValue = if (uIndex < uImage.size) (uImage[uIndex].toInt() and 0xFF) - 128 else 0
                    val vValue = if (vIndex < vImage.size) (vImage[vIndex].toInt() and 0xFF) - 128 else 0
                    
                    // Standard YUV to RGB conversion formula
                    val r = (yValue + 1.402 * vValue).toInt().coerceIn(0, 255)
                    val g = (yValue - 0.344136 * uValue - 0.714136 * vValue).toInt().coerceIn(0, 255)
                    val b = (yValue + 1.772 * uValue).toInt().coerceIn(0, 255)
                    
                    pixels[pixelIndex] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, imageProxy.width, 0, 0, imageProxy.width, imageProxy.height)
        
        // Apply rotation from ImageProxy metadata to correct orientation
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            val rotated = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            rotated
        } else {
            bitmap
        }
    } catch (e: Exception) {
        Log.e("UnifiedCameraPreview", "Error converting ImageProxy to Bitmap", e)
        null
    }
}
