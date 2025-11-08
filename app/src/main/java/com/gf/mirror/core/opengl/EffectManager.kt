package com.gf.mirror.core.opengl

/**
 * Effect manager interface for managing mirror effects
 */
interface EffectManager {
    /**
     * Get all available effects
     */
    fun getAvailableEffects(): List<MirrorEffect>
    
    /**
     * Get current effect
     */
    fun getCurrentEffect(): MirrorEffect?
    
    /**
     * Set current effect
     */
    fun setEffect(effect: MirrorEffect)
    
    /**
     * Create custom effect with configuration
     */
    fun createCustomEffect(config: EffectConfig): MirrorEffect
    
    /**
     * Get effect by ID
     */
    fun getEffectById(id: String): MirrorEffect?
    
    /**
     * Add new effect
     */
    fun addEffect(effect: MirrorEffect)
    
    /**
     * Remove effect by ID
     */
    fun removeEffect(id: String)
}

/**
 * Effect configuration data class
 */
data class EffectConfig(
    val name: String,
    val shaderCode: String,
    val parameters: Map<String, Float> = emptyMap()
)