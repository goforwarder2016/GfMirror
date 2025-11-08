package com.gf.mirror.ui.components

import android.graphics.SurfaceTexture
import android.view.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.gf.mirror.core.capture.ImageCaptureManager
import com.gf.mirror.core.common.LocalizationManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    localizationManager: LocalizationManager,
    imageCaptureManager: ImageCaptureManager? = null,
    onCameraReady: (PreviewView) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            println("GFMirror: CameraPreview - Getting camera provider...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            println("GFMirror: CameraPreview - Camera provider obtained successfully")
        } catch (e: Exception) {
            println("GFMirror: CameraPreview - Failed to get camera provider: ${e.message}")
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                onCameraReady(this)
            }
        },
        modifier = modifier,
        update = { previewView ->
            cameraProvider?.let { provider ->
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                // Create ImageCapture
                val imageCapture = ImageCapture.Builder()
                    .setTargetResolution(android.util.Size(1280, 720))
                    .build()
                
                // Set ImageCapture to manager
                imageCaptureManager?.setImageCapture(imageCapture)
                
                try {
                    println("GFMirror: CameraPreview - Binding camera to lifecycle...")
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                    println("GFMirror: CameraPreview - Camera bound successfully")
                } catch (e: Exception) {
                    println("GFMirror: CameraPreview - Camera binding failed: ${e.message}")
                }
            }
        }
    )
}

@Composable
fun CameraWithSurfaceTexture(
    modifier: Modifier = Modifier,
    surfaceTexture: SurfaceTexture?,
    imageCaptureManager: ImageCaptureManager? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    LaunchedEffect(Unit) {
        try {
            println("GFMirror: CameraWithSurfaceTexture - Getting camera provider...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            println("GFMirror: CameraWithSurfaceTexture - Camera provider obtained successfully")
        } catch (e: Exception) {
            println("GFMirror: CameraWithSurfaceTexture - Failed to get camera provider: ${e.message}")
        }
    }
    
    LaunchedEffect(surfaceTexture) {
        surfaceTexture?.let { st ->
            cameraProvider?.let { provider ->
                try {
                    println("GFMirror: CameraWithSurfaceTexture - Binding camera to SurfaceTexture...")
                    
                    val preview = Preview.Builder()
                        .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                    
                    // Create Surface and bind to SurfaceTexture
                    val surface = Surface(st)
                    preview.setSurfaceProvider { request ->
                        request.provideSurface(surface, cameraExecutor) {
                            surface.release()
                        }
                    }
                    
                    // Create ImageCapture
                    val imageCapture = ImageCapture.Builder()
                        .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                    
                    // Set ImageCapture to manager
                    imageCaptureManager?.setImageCapture(imageCapture)
                    
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                    println("GFMirror: CameraWithSurfaceTexture - Camera bound to SurfaceTexture successfully")
                } catch (e: Exception) {
                    println("GFMirror: CameraWithSurfaceTexture - Camera binding failed: ${e.message}")
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
