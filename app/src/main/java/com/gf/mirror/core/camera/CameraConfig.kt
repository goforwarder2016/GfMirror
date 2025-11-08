package com.gf.mirror.core.camera

import android.util.Size

/**
 * Camera configuration data class
 */
data class CameraConfig(
    val targetResolution: Size = Size(1280, 720),
    val targetFrameRate: Int = 30,
    val enableAutoFocus: Boolean = true,
    val enableFlash: Boolean = false,
    val isFrontCamera: Boolean = false
)