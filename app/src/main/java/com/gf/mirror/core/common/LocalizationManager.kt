package com.gf.mirror.core.common

import androidx.annotation.StringRes

/**
 * Localization manager interface for multi-language support
 * Follows Android internationalization standards
 */
interface LocalizationManager {
    /**
     * Get current language code
     */
    fun getCurrentLanguage(): String
    
    /**
     * Set language and trigger activity recreation
     */
    fun setLanguage(language: String)
    
    /**
     * Get localized string by resource ID
     */
    fun getString(@StringRes resId: Int): String
    
    /**
     * Get localized string with format arguments
     */
    fun getString(@StringRes resId: Int, vararg args: Any): String
    
    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<SupportedLanguage>
}

/**
 * Supported language enum
 */
enum class SupportedLanguage(val code: String, val displayName: String) {
    SIMPLIFIED_CHINESE("zh-rCN", "Simplified Chinese"),
    TRADITIONAL_CHINESE("zh-rTW", "Traditional Chinese"),
    ENGLISH("en", "English"),
    KOREAN("ko", "한국어"),
    JAPANESE("ja", "Japanese")
}