package com.gf.mirror.core.camera

import android.graphics.Bitmap
import android.net.Uri
import android.view.Surface
import android.util.Size

/**
 * Camera manager interface for handling camera operations
 */
interface CameraManager {
    /**
     * Initialize camera system
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Start camera preview on given surface
     */
    fun startPreview(surface: Surface)
    
    /**
     * Stop camera preview
     */
    fun stopPreview()
    
    /**
     * Switch between front and back camera
     */
    fun switchCamera(): Result<Unit>
    
    /**
     * Capture image from current frame
     */
    fun captureImage(): Result<Bitmap>
    
    /**
     * Start video recording
     */
    fun startVideoRecording(): Result<Unit>
    
    /**
     * Stop video recording and return URI
     */
    fun stopVideoRecording(): Result<Uri>
    
    /**
     * Release camera resources
     */
    fun release()
    
    /**
     * Get current camera configuration
     */
    fun getCurrentConfig(): CameraConfig
    
    /**
     * Update camera configuration
     */
    fun updateConfig(config: CameraConfig)
}