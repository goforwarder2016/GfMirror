package com.gf.mirror.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gf.mirror.core.opengl.MirrorEffect

/**
 * Effect preview component
 * Shows colors corresponding to different effects
 */
@Composable
fun EffectPreview(
    modifier: Modifier = Modifier,
    effect: MirrorEffect?
) {
    val backgroundColor = when (effect?.id) {
        "fisheye" -> Color.Red // Red
        "horizontal_stretch" -> Color.Green // Green
        "vertical_stretch" -> Color.Blue // Blue
        "twist" -> Color.Yellow // Yellow
        "bulge" -> Color.Magenta // Magenta
        "wave" -> Color.Cyan // Cyan
        else -> Color.Gray // Gray
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = 0.3f)) // Semi-transparent effect
    )
}
