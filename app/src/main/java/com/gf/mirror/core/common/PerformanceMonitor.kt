package com.gf.mirror.core.common

/**
 * Performance monitor interface
 */
interface PerformanceMonitor {
    /**
     * Start frame rate monitoring
     */
    fun startFrameRateMonitoring()
    
    /**
     * Stop frame rate monitoring
     */
    fun stopFrameRateMonitoring()
    
    /**
     * Get current frame rate
     */
    fun getCurrentFrameRate(): Float
    
    /**
     * Get memory usage information
     */
    fun getMemoryUsage(): MemoryInfo
    
    /**
     * Get CPU usage percentage
     */
    fun getCpuUsage(): Float
    
    /**
     * Get performance configuration
     */
    fun getPerformanceConfig(): PerformanceConfig
    
    /**
     * Update performance configuration
     */
    fun updatePerformanceConfig(config: PerformanceConfig)
}

/**
 * Memory information data class
 */
data class MemoryInfo(
    val usedMemory: Long,
    val maxMemory: Long,
    val freeMemory: Long,
    val memoryUsagePercentage: Float
)

/**
 * Performance configuration data class
 */
data class PerformanceConfig(
    val targetFrameRate: Int = 30,
    val maxMemoryUsage: Long = 200 * 1024 * 1024, // 200MB
    val enablePerformanceLogging: Boolean = false
)