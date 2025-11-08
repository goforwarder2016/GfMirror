package com.gf.mirror.core.opengl

import com.gf.mirror.R
import com.gf.mirror.core.opengl.effects.MirrorEffects

/**
 * Default implementation of EffectManager
 */
class DefaultEffectManager : EffectManager {
    
    private val effects = mutableListOf<MirrorEffect>()
    private var currentEffect: MirrorEffect? = null
    
    init {
        initializeDefaultEffects()
    }
    
    private fun initializeDefaultEffects() {
        // Use predefined effects
        MirrorEffects.getAllEffects().forEach { effect ->
            addEffect(effect.copy(previewIcon = R.drawable.ic_effect_custom))
        }
    }
    
    override fun getAvailableEffects(): List<MirrorEffect> {
        return effects.toList()
    }
    
    override fun getCurrentEffect(): MirrorEffect? {
        return currentEffect
    }
    
    override fun setEffect(effect: MirrorEffect) {
        currentEffect = effect
    }
    
    override fun createCustomEffect(config: EffectConfig): MirrorEffect {
        val effect = MirrorEffect(
            id = "custom_${System.currentTimeMillis()}",
            name = config.name,
            shaderCode = config.shaderCode,
            parameters = config.parameters,
            previewIcon = R.drawable.ic_effect_custom
        )
        addEffect(effect)
        return effect
    }
    
    override fun getEffectById(id: String): MirrorEffect? {
        return effects.find { it.id == id }
    }
    
    override fun addEffect(effect: MirrorEffect) {
        // Only add effects with valid ID and name
        if (effect.id.isNotBlank() && effect.name.isNotBlank()) {
            effects.add(effect)
        }
    }
    
    override fun removeEffect(id: String) {
        effects.removeAll { it.id == id }
        if (currentEffect?.id == id) {
            currentEffect = effects.firstOrNull()
        }
    }
    
}