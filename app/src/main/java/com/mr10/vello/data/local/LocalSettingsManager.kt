package com.mr10.vello.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vello_settings", Context.MODE_PRIVATE)

    private val _themeIndex = MutableStateFlow(prefs.getInt(KEY_THEME_INDEX, 0))
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    private val _fontSize = MutableStateFlow(prefs.getString(KEY_FONT_SIZE, "Medium") ?: "Medium")
    val fontSize: StateFlow<String> = _fontSize.asStateFlow()

    private val _wallpaperUri = MutableStateFlow(prefs.getString(KEY_WALLPAPER_URI, null))
    val wallpaperUri: StateFlow<String?> = _wallpaperUri.asStateFlow()

    private val _wallpaperOpacity = MutableStateFlow(prefs.getFloat(KEY_WALLPAPER_OPACITY, 0.4f))
    val wallpaperOpacity: StateFlow<Float> = _wallpaperOpacity.asStateFlow()

    private val _enterIsSend = MutableStateFlow(prefs.getBoolean(KEY_ENTER_IS_SEND, false))
    val enterIsSend: StateFlow<Boolean> = _enterIsSend.asStateFlow()

    private val _notificationSoundIndex = MutableStateFlow(prefs.getInt(KEY_NOTIFICATION_SOUND, 0))
    val notificationSoundIndex: StateFlow<Int> = _notificationSoundIndex.asStateFlow()

    private val _ringtoneIndex = MutableStateFlow(prefs.getInt(KEY_RINGTONE, 0))
    val ringtoneIndex: StateFlow<Int> = _ringtoneIndex.asStateFlow()

    private val _highPriorityEnabled = MutableStateFlow(prefs.getBoolean(KEY_HIGH_PRIORITY, true))
    val highPriorityEnabled: StateFlow<Boolean> = _highPriorityEnabled.asStateFlow()

    fun setThemeIndex(index: Int) {
        prefs.edit().putInt(KEY_THEME_INDEX, index).apply()
        _themeIndex.value = index
    }

    fun setFontSize(size: String) {
        prefs.edit().putString(KEY_FONT_SIZE, size).apply()
        _fontSize.value = size
    }

    fun setWallpaperUri(uri: String?) {
        prefs.edit().putString(KEY_WALLPAPER_URI, uri).apply()
        _wallpaperUri.value = uri
    }

    fun setWallpaperOpacity(opacity: Float) {
        prefs.edit().putFloat(KEY_WALLPAPER_OPACITY, opacity).apply()
        _wallpaperOpacity.value = opacity
    }

    fun setEnterIsSend(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENTER_IS_SEND, enabled).apply()
        _enterIsSend.value = enabled
    }

    fun setNotificationSoundIndex(index: Int) {
        prefs.edit().putInt(KEY_NOTIFICATION_SOUND, index).apply()
        _notificationSoundIndex.value = index
    }

    fun setRingtoneIndex(index: Int) {
        prefs.edit().putInt(KEY_RINGTONE, index).apply()
        _ringtoneIndex.value = index
    }

    fun setHighPriorityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_PRIORITY, enabled).apply()
        _highPriorityEnabled.value = enabled
    }

    companion object {
        private const val KEY_THEME_INDEX = "theme_index"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_WALLPAPER_OPACITY = "wallpaper_opacity"
        private const val KEY_ENTER_IS_SEND = "enter_is_send"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_RINGTONE = "ringtone"
        private const val KEY_HIGH_PRIORITY = "high_priority"

        @Volatile
        private var INSTANCE: LocalSettingsManager? = null

        fun getInstance(context: Context): LocalSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalSettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
