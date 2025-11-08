package com.gf.mirror

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

/**
 * Application class for GFMirror app
 * Handles proper locale configuration for internationalization
 */
class GFMirrorApplication : Application() {
    
    companion object {
        private const val PREF_LANGUAGE = "pref_language"
        private const val DEFAULT_LANGUAGE = "en"
        
        /**
         * Get the saved language preference
         * Force English as default if no language preference is saved
         */
        fun getSavedLanguage(context: Context): String {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val savedLanguage = prefs.getString(PREF_LANGUAGE, null)
            
            // If no language preference is saved, force English as default
            if (savedLanguage == null) {
                println("GFMirror: No saved language found, setting default to English")
                saveLanguage(context, DEFAULT_LANGUAGE)
                return DEFAULT_LANGUAGE
            }
            
            println("GFMirror: Using saved language: $savedLanguage")
            return savedLanguage
        }
        
        /**
         * Save language preference
         */
        fun saveLanguage(context: Context, language: String) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_LANGUAGE, language).apply()
        }
        
        /**
         * Apply locale to context
         */
        fun applyLocale(context: Context, language: String): Context {
            val locale = when (language) {
                "zh-rCN" -> Locale.SIMPLIFIED_CHINESE
                "zh-rTW" -> Locale.TRADITIONAL_CHINESE
                "ko" -> Locale.KOREAN
                "ja" -> Locale.JAPANESE
                else -> Locale.ENGLISH
            }
            
            println("GFMirror: Applying locale: $language -> $locale")
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        // Apply saved language on app startup
        val savedLanguage = getSavedLanguage(this)
        applyLocale(this, savedLanguage)
    }
    
    override fun attachBaseContext(base: Context) {
        // Force English as default language on first launch
        val savedLanguage = getSavedLanguage(base)
        super.attachBaseContext(applyLocale(base, savedLanguage))
    }
}
