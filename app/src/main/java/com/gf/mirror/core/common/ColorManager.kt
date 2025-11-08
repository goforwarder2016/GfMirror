package com.gf.mirror.core.common

import androidx.compose.ui.graphics.Color

/**
 * Centralized color management to avoid duplicate color definitions
 */
object ColorManager {
    
    // Primary colors
    val White = Color.White
    val Black = Color.Black
    val Red = Color.Red
    val Green = Color.Green
    val Blue = Color.Blue
    val Gray = Color.Gray
    
    // Custom colors with alpha
    val BlackWithAlpha = Color.Black.copy(alpha = 0.8f)
    val BlackWithLightAlpha = Color.Black.copy(alpha = 0.5f)
    val GrayWithAlpha = Color.Gray.copy(alpha = 0.3f)
    
    // Status colors
    val CameraActiveColor = Color.Red
    val CameraInactiveColor = Color.Green
    val CaptureButtonColor = Color.Blue
    val SelectedEffectColor = Color.Blue
    val UnselectedEffectColor = Color.Gray.copy(alpha = 0.3f)
}