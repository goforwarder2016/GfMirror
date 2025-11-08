package com.gf.mirror.core.common

/**
 * Factory for creating common exceptions to avoid duplicate exception creation
 */
object ExceptionFactory {
    
    /**
     * Create IllegalStateException for uninitialized ImageCapture
     */
    fun createImageCaptureNotInitializedException(): IllegalStateException {
        return IllegalStateException("ImageCapture not initialized")
    }
    
    /**
     * Create IllegalStateException for uninitialized VideoCapture
     */
    fun createVideoCaptureNotInitializedException(): IllegalStateException {
        return IllegalStateException("VideoCapture not initialized")
    }
    
    /**
     * Create IllegalStateException for uninitialized CameraProvider
     */
    fun createCameraProviderNotInitializedException(): IllegalStateException {
        return IllegalStateException("CameraProvider not initialized")
    }
}