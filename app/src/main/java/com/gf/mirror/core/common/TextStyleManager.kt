package com.gf.mirror.core.common

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * Centralized text style management to avoid duplicate text style definitions
 */
object TextStyleManager {
    
    // Font weights
    val Bold = FontWeight.Bold
    val Medium = FontWeight.Medium
    val Normal = FontWeight.Normal
    
    // Text alignments
    val TextCenter = TextAlign.Center
    val TextStart = TextAlign.Start
    val TextEnd = TextAlign.End

    // Layout alignments
    val CenterHorizontally = Alignment.CenterHorizontally
    val CenterVertically = Alignment.CenterVertically
    val Center = Alignment.Center
}