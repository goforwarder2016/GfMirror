package com.gf.mirror.core.capture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Image capture manager interface
 */
interface ImageCaptureManager {
    /**
     * Capture image
     */
    suspend fun captureImage(): Result<Bitmap>
    
    /**
     * Save image to gallery
     */
    suspend fun saveImageToGallery(bitmap: Bitmap): Result<Uri>
    
    /**
     * Set image capture configuration
     */
    fun setImageCapture(imageCapture: ImageCapture?)
}

/**
 * Default image capture manager implementation
 */
class DefaultImageCaptureManager(
    private val context: Context
) : ImageCaptureManager {
    
    private var imageCapture: ImageCapture? = null
    
    override fun setImageCapture(imageCapture: ImageCapture?) {
        this.imageCapture = imageCapture
    }
    
    override suspend fun captureImage(): Result<Bitmap> = suspendCancellableCoroutine { continuation ->
        val imageCapture = this.imageCapture ?: run {
            continuation.resume(Result.failure(IllegalStateException("ImageCapture not initialized")))
            return@suspendCancellableCoroutine
        }
        
        // Create output file
        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "GFMirror_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        // Read Bitmap from file
                        val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
                        if (bitmap != null) {
                            continuation.resume(Result.success(bitmap))
                        } else {
                            continuation.resume(Result.failure(Exception("Failed to decode captured image")))
                        }
                    } catch (e: Exception) {
                        continuation.resume(Result.failure(e))
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(Result.failure(exception))
                }
            }
        )
    }
    
    override suspend fun saveImageToGallery(bitmap: Bitmap): Result<Uri> = suspendCancellableCoroutine { continuation ->
        try {
            println("ImageCaptureManager: Starting to save bitmap to gallery, size: ${bitmap.width}x${bitmap.height}")
            
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "GFMirror_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GFMirror")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            
            println("ImageCaptureManager: ContentValues created: ${contentValues.getAsString(MediaStore.Images.Media.DISPLAY_NAME)}")
            
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            if (uri != null) {
                println("ImageCaptureManager: URI created: $uri")
                resolver.openOutputStream(uri)?.use { outputStream ->
                    val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    println("ImageCaptureManager: Bitmap compressed: $compressed")
                }
                
                // Mark as completed to make it visible in gallery
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                
                println("ImageCaptureManager: Image saved successfully to gallery: $uri")
                continuation.resume(Result.success(uri))
            } else {
                println("ImageCaptureManager: Failed to create URI")
                continuation.resume(Result.failure(Exception("Failed to create image file in gallery")))
            }
        } catch (e: Exception) {
            println("ImageCaptureManager: Exception while saving: ${e.message}")
            e.printStackTrace()
            continuation.resume(Result.failure(e))
        }
    }
}
