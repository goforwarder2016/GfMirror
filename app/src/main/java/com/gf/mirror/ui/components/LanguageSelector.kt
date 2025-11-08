package com.gf.mirror.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gf.mirror.R
import com.gf.mirror.core.common.LocalizationManager
import com.gf.mirror.core.common.SupportedLanguage
import com.gf.mirror.core.common.ColorManager
import com.gf.mirror.core.common.SizeManager
import com.gf.mirror.core.common.TextStyleManager

/**
 * Language selector dialog component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    localizationManager: LocalizationManager,
    onLanguageSelected: (String) -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ColorManager.BlackWithAlpha
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = localizationManager.getString(R.string.language),
                        fontSize = SizeManager.LargeFontSize,
                        fontWeight = TextStyleManager.Bold,
                        color = ColorManager.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Language list
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(localizationManager.getSupportedLanguages()) { language ->
                            LanguageItem(
                                language = language,
                                isSelected = localizationManager.getCurrentLanguage() == language.code,
                                onClick = {
                                    onLanguageSelected(language.code)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual language item in the selector
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageItem(
    language: SupportedLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ColorManager.SelectedEffectColor else ColorManager.UnselectedEffectColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.displayName,
                color = ColorManager.White,
                fontSize = SizeManager.MediumFontSize,
                fontWeight = if (isSelected) TextStyleManager.Bold else TextStyleManager.Normal,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Text(
                    text = "✓",
                    color = ColorManager.White,
                    fontSize = SizeManager.LargeFontSize,
                    fontWeight = TextStyleManager.Bold
                )
            }
        }
    }
}

/**
 * Language selector button for the main interface
 */
@Composable
fun LanguageSelectorButton(
    localizationManager: LocalizationManager,
    onLanguageSelected: (String) -> Unit
) {
    var showLanguageSelector by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showLanguageSelector = true },
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorManager.CameraInactiveColor
        )
    ) {
        Text(
            text = localizationManager.getString(R.string.language),
            color = ColorManager.White
        )
    }
    
    LanguageSelector(
        isVisible = showLanguageSelector,
        onDismiss = { showLanguageSelector = false },
        localizationManager = localizationManager,
        onLanguageSelected = onLanguageSelected
    )
}
