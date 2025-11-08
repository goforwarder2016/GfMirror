package com.gf.mirror.core.common

/**
 * Configuration manager interface
 */
interface ConfigManager {
    /**
     * Get configuration value by key
     */
    fun <T> getConfig(key: String, defaultValue: T): T
    
    /**
     * Set configuration value
     */
    fun <T> setConfig(key: String, value: T)
    
    /**
     * Reset all configurations to defaults
     */
    fun resetToDefaults()
    
    /**
     * Export configuration as JSON string
     */
    fun exportConfig(): String
    
    /**
     * Import configuration from JSON string
     */
    fun importConfig(configJson: String)
    
    /**
     * Check if configuration key exists
     */
    fun hasConfig(key: String): Boolean
    
    /**
     * Remove configuration by key
     */
    fun removeConfig(key: String)
}