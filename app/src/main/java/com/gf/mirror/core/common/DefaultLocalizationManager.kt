package com.gf.mirror.core.common

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.gf.mirror.GFMirrorApplication
import java.util.*

/**
 * Default implementation of LocalizationManager
 * Follows Android internationalization standards
 */
class DefaultLocalizationManager(
    private val context: Context
) : LocalizationManager {
    
    override fun getCurrentLanguage(): String {
        return GFMirrorApplication.getSavedLanguage(context)
    }
    
    override fun setLanguage(language: String) {
        if (isValidLanguage(language)) {
            // Save language preference
            GFMirrorApplication.saveLanguage(context, language)
            
            // If context is an Activity, recreate it to apply locale changes
            if (context is Activity) {
                context.recreate()
            }
        }
    }
    
    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }
    
    override fun getString(@StringRes resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
    
    override fun getSupportedLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().toList()
    }
    
    private fun isValidLanguage(language: String): Boolean {
        return SupportedLanguage.values().any { it.code == language }
    }
}