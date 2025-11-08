package com.gf.mirror.core.common

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * Centralized executor management to avoid duplicate executor creation
 */
object ExecutorManager {
    
    private val _cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }
    
    private val _performanceExecutor: ScheduledExecutorService by lazy {
        Executors.newSingleThreadScheduledExecutor()
    }
    
    /**
     * Get camera executor for camera operations
     */
    fun getCameraExecutor(): ExecutorService = _cameraExecutor
    
    /**
     * Get performance monitoring executor
     */
    fun getPerformanceExecutor(): ScheduledExecutorService = _performanceExecutor
    
    /**
     * Shutdown all executors
     */
    fun shutdownAll() {
        _cameraExecutor.shutdown()
        _performanceExecutor.shutdown()
    }
}