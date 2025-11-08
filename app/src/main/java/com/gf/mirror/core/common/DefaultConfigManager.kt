package com.gf.mirror.core.common

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Default implementation of ConfigManager using SharedPreferences
 */
class DefaultConfigManager(
    private val context: Context
) : ConfigManager {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "gfmirror_config", 
        Context.MODE_PRIVATE
    )
    
    override fun <T> getConfig(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue) as T
            is Long -> prefs.getLong(key, defaultValue) as T
            is Float -> prefs.getFloat(key, defaultValue) as T
            is Boolean -> prefs.getBoolean(key, defaultValue) as T
            else -> defaultValue
        }
    }
    
    override fun <T> setConfig(key: String, value: T) {
        val editor = prefs.edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Boolean -> editor.putBoolean(key, value)
        }
        editor.apply()
    }
    
    override fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
    
    override fun exportConfig(): String {
        val json = JSONObject()
        val allPrefs = prefs.all
        
        for ((key, value) in allPrefs) {
            when (value) {
                is String -> json.put(key, value)
                is Int -> json.put(key, value)
                is Long -> json.put(key, value)
                is Float -> json.put(key, value.toDouble())
                is Boolean -> json.put(key, value)
            }
        }
        
        return json.toString()
    }
    
    override fun importConfig(configJson: String) {
        try {
            val json = JSONObject(configJson)
            val editor = prefs.edit()
            
            for (key in json.keys()) {
                val value = json.get(key)
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is Boolean -> editor.putBoolean(key, value)
                }
            }
            editor.apply()
        } catch (e: Exception) {
            // Handle JSON parsing error
        }
    }
    
    override fun hasConfig(key: String): Boolean {
        return prefs.contains(key)
    }
    
    override fun removeConfig(key: String) {
        prefs.edit().remove(key).apply()
    }
}