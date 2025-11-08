package com.gf.mirror.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import com.gf.mirror.R
import com.gf.mirror.core.common.ConfigManager
import com.gf.mirror.core.common.LocalizationManager
import com.gf.mirror.core.common.PerformanceMonitor
import com.gf.mirror.core.common.ColorManager
import com.gf.mirror.core.common.SizeManager
import com.gf.mirror.core.common.TextStyleManager
import com.gf.mirror.core.opengl.EffectManager
import com.gf.mirror.core.opengl.MirrorEffect
import com.gf.mirror.core.camera.CameraManager
import com.gf.mirror.core.capture.ImageCaptureManager
import com.gf.mirror.ui.components.UnifiedCameraPreview
import com.gf.mirror.ui.components.SimpleOpenGLMirrorView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

/**
 * Check if bitmap is completely black
 */
private fun isBitmapBlack(bitmap: android.graphics.Bitmap): Boolean {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    
    // Check if the first 100 pixels are all black or near black
    val sampleSize = kotlin.math.min(100, pixels.size)
    for (i in 0 until sampleSize) {
        val pixel = pixels[i]
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        
        // If not black or near black, return false
        if (r > 10 || g > 10 || b > 10) {
            return false
        }
    }
    return true
}

/**
 * Get localized effect name by effect ID
 */
private fun getLocalizedEffectName(effectId: String, localizationManager: LocalizationManager): String {
    // Filter out empty or null effect IDs
    if (effectId.isBlank()) {
        return "Unknown Effect"
    }
    
    val localizedName = when (effectId) {
        "fisheye" -> localizationManager.getString(R.string.effect_fisheye)
        "barrel" -> localizationManager.getString(R.string.effect_barrel)
        "pincushion" -> localizationManager.getString(R.string.effect_pincushion)
        "ripple" -> localizationManager.getString(R.string.effect_ripple)
        "slim_face" -> localizationManager.getString(R.string.effect_slim_face)
        "stretch" -> localizationManager.getString(R.string.effect_stretch)
        "distort" -> localizationManager.getString(R.string.effect_distort)
        "mirror" -> localizationManager.getString(R.string.effect_mirror)
        "horizontal_stretch" -> localizationManager.getString(R.string.effect_horizontal_stretch)
        "vertical_stretch" -> localizationManager.getString(R.string.effect_vertical_stretch)
        "twist" -> localizationManager.getString(R.string.effect_twist)
        "bulge" -> localizationManager.getString(R.string.effect_bulge)
        "wave" -> localizationManager.getString(R.string.effect_wave)
        "custom" -> localizationManager.getString(R.string.effect_custom)
        else -> effectId // Fallback to ID if no localized name found
    }
    
    // Return fallback name if localized name is empty or blank
    return if (localizedName.isBlank()) {
        when (effectId) {
            "fisheye" -> "Fisheye"
            "barrel" -> "Barrel"
            "pincushion" -> "Pincushion"
            "ripple" -> "Ripple"
            "slim_face" -> "Slim Face"
            "stretch" -> "Stretch"
            "distort" -> "Distort"
            "mirror" -> "Mirror"
            "horizontal_stretch" -> "Horizontal Stretch"
            "vertical_stretch" -> "Vertical Stretch"
            "twist" -> "Twist"
            "bulge" -> "Bulge"
            "wave" -> "Wave"
            "custom" -> "Custom"
            else -> effectId
        }
    } else {
        localizedName
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    configManager: ConfigManager,
    localizationManager: LocalizationManager,
    performanceMonitor: PerformanceMonitor,
    effectManager: EffectManager,
    cameraManager: CameraManager,
    imageCaptureManager: ImageCaptureManager
) {
    var selectedEffect by remember { mutableStateOf<MirrorEffect?>(null) }
    var isCameraActive by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var simpleOpenGLMirrorView by remember { mutableStateOf<SimpleOpenGLMirrorView?>(null) }
    var currentBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var currentEffectIndex by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    // Removed showEffectChangeHint - no more toast notifications
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEffectPreviewDialog by remember { mutableStateOf(false) }
    var isButtonPressed by remember { mutableStateOf(false) }
    // Removed showOriginalPreview - no longer needed
    var isFrontCamera by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) } // Default use back camera
    
    val coroutineScope = rememberCoroutineScope()
    val availableEffects = remember { effectManager.getAvailableEffects() }
    
           // Initialize camera and set default effect
           LaunchedEffect(Unit) {
               // CameraPreview component will handle its own initialization
               // We just need to set cameraInitialized to true to allow camera activation
               println("GFMirror: Setting camera as available")
               cameraInitialized = true
               isCameraActive = true // Activate camera immediately on app start
               
               // Set default effect to hide original preview
               if (availableEffects.isNotEmpty() && selectedEffect == null) {
                   selectedEffect = availableEffects[0] // Set first effect as default
                   effectManager.setEffect(availableEffects[0])
                   println("GFMirror: Set default effect: ${availableEffects[0].name}")
               }
           }
    
    // Initialize with no effect (show original preview by default)
    LaunchedEffect(effectManager) {
        val effects = effectManager.getAvailableEffects()
        if (effects.isNotEmpty()) {
            // Don't set default effect - keep selectedEffect as null to show original preview
            currentEffectIndex = 0 // Set index to first effect for when user switches
            println("GFMirror: No default effect set, showing original preview")
        }
    }
    
    // Helper functions for effect switching
    fun switchToNextEffect() {
        if (availableEffects.isNotEmpty()) {
            currentEffectIndex = (currentEffectIndex + 1) % availableEffects.size
            val newEffect = availableEffects[currentEffectIndex]
            selectedEffect = newEffect
            effectManager.setEffect(newEffect)
            println("GFMirror: Switched to next effect: ${newEffect.name}")
        }
    }
    
    fun switchToPreviousEffect() {
        if (availableEffects.isNotEmpty()) {
            currentEffectIndex = if (currentEffectIndex == 0) {
                availableEffects.size - 1
            } else {
                currentEffectIndex - 1
            }
            val newEffect = availableEffects[currentEffectIndex]
            selectedEffect = newEffect
            effectManager.setEffect(newEffect)
            println("GFMirror: Switched to previous effect: ${newEffect.name}")
        }
    }
    
    // Full-screen effect view with gesture support
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { /* Handle drag end if needed */ }
                ) { change, _ ->
                    // Handle swipe gestures
                    val deltaX = change.position.x - change.previousPosition.x
                    val deltaY = change.position.y - change.previousPosition.y
                    
                    // Only trigger if horizontal movement is significantly larger than vertical
                    if (kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY) && kotlin.math.abs(deltaX) > 80) {
                        if (deltaX > 0) {
                            // Swipe right - previous effect
                            switchToPreviousEffect()
                        } else {
                            // Swipe left - next effect
                            switchToNextEffect()
                        }
                    }
                }
            }
    ) {
        // Full-screen camera preview with effects
        if (isCameraActive && cameraInitialized) {
            Box(modifier = Modifier.fillMaxSize()) {
                // OpenGL effect view
                AndroidView(
                    factory = { context ->
                        println("GFMirror: Creating SimpleOpenGLMirrorView")
                        SimpleOpenGLMirrorView(context).also { view ->
                            simpleOpenGLMirrorView = view
                            // Set mirror effect as default (simpler effect)
                            val mirrorEffect = availableEffects.find { it.id == "mirror" }
                            if (mirrorEffect != null) {
                                view.setEffect(mirrorEffect)
                                println("GFMirror: SimpleOpenGLMirrorView created with mirror effect")
                            } else {
                                println("GFMirror: SimpleOpenGLMirrorView created, mirror effect not found")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mirrorView ->
                        selectedEffect?.let { effect ->
                            // Only update effect if it actually changed
                            if (mirrorView.currentEffect?.id != effect.id) {
                                println("GFMirror: Updating SimpleOpenGLMirrorView with effect: ${effect.name}")
                                mirrorView.setEffect(effect)
                            }
                        }
                        // Update bitmap - always call updateBitmap to ensure data flow
                        currentBitmap?.let { bitmap ->
                            mirrorView.updateBitmap(bitmap)
                        } ?: run {
                            mirrorView.updateBitmap(null)
                        }
                    }
                )
                
                // Camera preview for data (hidden, only for bitmap updates)
                UnifiedCameraPreview(
                    localizationManager = localizationManager,
                    imageCaptureManager = imageCaptureManager,
                    onBitmapUpdate = { bitmap ->
                        if (bitmap != null) {
                            currentBitmap = bitmap
                            println("GFMirror: Bitmap updated, size: ${bitmap.width}x${bitmap.height}")
                            // Directly update the OpenGL view to ensure immediate data flow
                            simpleOpenGLMirrorView?.updateBitmap(bitmap)
                            println("GFMirror: Bitmap directly updated to OpenGL view")
                        } else {
                            println("GFMirror: Received null bitmap")
                            currentBitmap = null
                            simpleOpenGLMirrorView?.updateBitmap(null)
                        }
                    },
                    showSmallPreview = false, // Always hide small preview - no longer needed
                    isFrontCamera = isFrontCamera // Pass camera selection state
                )
                
                // Small original preview in bottom-left corner (handled by UnifiedCameraPreview)
                // Removed to avoid double camera binding - now handled by showSmallPreview parameter
                
                // Bottom button area - effect switch, capture button, and camera switch button
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left effect switch button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ColorManager.Black.copy(alpha = 0.6f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // Switch to next effect
                                switchToNextEffect()
                                println("GFMirror: Effect switch button clicked")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = localizationManager.getString(R.string.switch_effect),
                            tint = ColorManager.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Center capture button
                    Box(
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isButtonPressed) Color.Gray else Color.White,
                                    CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    // Add visual feedback
                                    isButtonPressed = true
                                    
                                    // Capture photo with current effect
                                    coroutineScope.launch {
                                        try {
                                            isCapturing = true
                                            val bitmap = simpleOpenGLMirrorView?.captureEffectBitmap()
                                            if (bitmap != null) {
                                                val result = imageCaptureManager.saveImageToGallery(bitmap)
                                                result.fold(
                                                    onSuccess = { uri ->
                                                        println("GFMirror: Photo saved successfully: $uri")
                                                    },
                                                    onFailure = { error ->
                                                        println("GFMirror: Failed to save photo: ${error.message}")
                                                    }
                                                )
                                            }
                                        } catch (e: Exception) {
                                            println("GFMirror: Error capturing photo: ${e.message}")
                                        } finally {
                                            isCapturing = false
                                            // Reset button state after a short delay
                                            kotlinx.coroutines.delay(200)
                                            isButtonPressed = false
                                        }
                                    }
                                }
                        )
                    }
                    
                    // Right camera switch button (simplified version)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ColorManager.Black.copy(alpha = 0.6f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // Switch camera with rotation animation
                                isFrontCamera = !isFrontCamera
                                rotationAngle += 180f
                                println("GFMirror: Camera switched to ${if (isFrontCamera) "front" else "back"}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchCamera,
                            contentDescription = localizationManager.getString(R.string.switch_camera),
                            tint = ColorManager.White,
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (cameraInitialized) 
                        localizationManager.getString(R.string.camera_preview_placeholder)
                    else 
                        localizationManager.getString(R.string.initializing_camera),
                    color = ColorManager.White,
                    fontSize = SizeManager.LargeFontSize,
                    textAlign = TextAlign.Center
                )
            }
        }
        
               // Top overlay with effect name and settings
               Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(16.dp),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.Top
               ) {
                   // Effect name in top-left corner
                   Text(
                       text = selectedEffect?.let { getLocalizedEffectName(it.id, localizationManager) } ?: "",
                       fontSize = 18.sp,
                       fontWeight = FontWeight.Bold,
                       color = ColorManager.White,
                       modifier = Modifier
                           .background(
                               Color.Black.copy(alpha = 0.5f),
                               CircleShape
                           )
                           .padding(horizontal = 12.dp, vertical = 6.dp)
                   )
                   
                   // Settings icon in top-right corner
                   IconButton(
                       onClick = {
                           showSettingsDialog = true
                           println("GFMirror: Settings button clicked")
                       },
                       modifier = Modifier
                           .background(
                               Color.Black.copy(alpha = 0.5f),
                               CircleShape
                           )
                   ) {
                       Icon(
                           imageVector = Icons.Default.Settings,
                           contentDescription = localizationManager.getString(R.string.settings),
                           tint = ColorManager.White
                       )
                   }
               }
            
            // Removed effect change hint - no more toast notifications
    }
    
    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = localizationManager.getString(R.string.settings),
                    fontSize = SizeManager.LargeFontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "${localizationManager.getString(R.string.current_effect)} ${selectedEffect?.let { getLocalizedEffectName(it.id, localizationManager) } ?: localizationManager.getString(R.string.none)}",
                        fontSize = SizeManager.MediumFontSize,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${localizationManager.getString(R.string.available_effects)} ${availableEffects.size}",
                        fontSize = SizeManager.MediumFontSize,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${localizationManager.getString(R.string.camera_status)} ${if (isCameraActive) localizationManager.getString(R.string.active) else localizationManager.getString(R.string.inactive)}",
                        fontSize = SizeManager.MediumFontSize,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Effect preview button
                    Button(
                        onClick = {
                            showSettingsDialog = false
                            showEffectPreviewDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(localizationManager.getString(R.string.effect_preview))
                    }
                    
                    // Language setting button
                    Button(
                        onClick = {
                            showSettingsDialog = false
                            showLanguageDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(localizationManager.getString(R.string.language_settings))
                    }
                    
                    // Original preview toggle - removed as no longer needed
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSettingsDialog = false }
                ) {
                    Text(localizationManager.getString(R.string.ok))
                }
            }
        )
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = localizationManager.getString(R.string.select_language),
                    fontSize = SizeManager.LargeFontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    val languages = localizationManager.getSupportedLanguages()
                    languages.forEach { lang ->
                        val displayName = lang.displayName
                        val code = lang.code
                        Button(
                            onClick = {
                                localizationManager.setLanguage(code)
                                showLanguageDialog = false
                                println("GFMirror: Language changed to $displayName ($code)")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showLanguageDialog = false }
                ) {
                    Text(localizationManager.getString(R.string.cancel))
                }
            }
        )
    }
    
    // Effect Preview Dialog
    if (showEffectPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showEffectPreviewDialog = false },
            title = {
                Text(
                    text = localizationManager.getString(R.string.effect_preview),
                    fontSize = SizeManager.LargeFontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // Limit maximum height
                ) {
                    Text(
                        text = localizationManager.getString(R.string.available_effects),
                        fontSize = SizeManager.MediumFontSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            availableEffects.filter { effect -> 
                                effect.id.isNotBlank() && effect.name.isNotBlank()
                            }
                        ) { effect ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        // Switch to selected effect
                                        selectedEffect = effect
                                        effectManager.setEffect(effect)
                                        currentEffectIndex = availableEffects.indexOf(effect)
                                        showEffectPreviewDialog = false
                                        println("GFMirror: Switched to effect: ${effect.name}")
                                    }
                            ) {
                                Text(
                                    text = getLocalizedEffectName(effect.id, localizationManager),
                                    fontSize = SizeManager.MediumFontSize,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showEffectPreviewDialog = false }
                ) {
                    Text(localizationManager.getString(R.string.close))
                }
            }
        )
    }
}

/**
 * Original camera preview component for showing raw camera feed
 * Used in small preview window without any effects
 */
@Composable
fun OriginalCameraPreview(
    modifier: Modifier = Modifier,
    localizationManager: LocalizationManager,
    imageCaptureManager: ImageCaptureManager? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isCameraBound by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { 
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    
    LaunchedEffect(Unit) {
        try {
            println("OriginalCameraPreview: Getting camera provider...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            println("OriginalCameraPreview: Camera provider obtained successfully")
        } catch (e: Exception) {
            println("OriginalCameraPreview: Failed to get camera provider: ${e.message}")
        }
    }
    
    LaunchedEffect(cameraProvider) {
        cameraProvider?.let { provider ->
            if (!isCameraBound) {
                try {
                    println("OriginalCameraPreview: Binding camera to lifecycle...")
                    isCameraBound = true
                
                    // Create Preview for original camera feed
                    val preview = Preview.Builder()
                        .setTargetResolution(android.util.Size(720, 720))
                        .build()
                    
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    
                    // Create ImageCapture
                    val imageCapture = ImageCapture.Builder()
                        .build()
                    
                    // Set ImageCapture to manager
                    imageCaptureManager?.setImageCapture(imageCapture)
                    
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                    println("OriginalCameraPreview: Camera bound successfully")
                } catch (e: Exception) {
                    println("OriginalCameraPreview: Camera binding failed: ${e.message}")
                    isCameraBound = false
                }
            }
        }
    }
    
    AndroidView(
        factory = { context ->
            previewView
        },
        modifier = modifier
    )
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}