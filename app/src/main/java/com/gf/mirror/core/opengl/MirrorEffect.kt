package com.gf.mirror.core.opengl

/**
 * Mirror effect data class
 */
data class MirrorEffect(
    val id: String,
    val name: String,
    val shaderCode: String,
    val parameters: Map<String, Float> = emptyMap(),
    val previewIcon: Int
)

/**
 * Effect parameter data class
 */
data class EffectParameter(
    val name: String,
    val type: ParameterType,
    val minValue: Float,
    val maxValue: Float,
    val defaultValue: Float
)

/**
 * Parameter type enum
 */
enum class ParameterType {
    FLOAT,
    INT,
    BOOLEAN
}