package com.eliasgreen18.vocabularytracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_KEEP_BACKUP_HISTORY = "keep_backup_history"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        private const val KEY_AUTO_SCROLL_ENABLED = "auto_scroll_enabled"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_GOOGLE_ACCOUNT_NAME = "google_account_name"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_AUTO_SPEAK_ENABLED = "auto_speak_enabled"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_READER_THEME = "reader_theme"
        private const val KEY_READER_FONT_SIZE = "reader_font_size"
        private const val KEY_USER_NAME = "user_name"
    }

    private fun <T> preferenceFlow(key: String, defaultValue: T): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, k ->
            if (k == key) {
                @Suppress("UNCHECKED_CAST")
                val value = when (defaultValue) {
                    is Boolean -> p.getBoolean(k, defaultValue) as T
                    is Int -> p.getInt(k, defaultValue) as T
                    is String -> p.getString(k, defaultValue) as T
                    else -> throw IllegalArgumentException("Unsupported type")
                }
                trySend(value)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart {
        @Suppress("UNCHECKED_CAST")
        val value = when (defaultValue) {
            is Boolean -> prefs.getBoolean(key, defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue) as T
            is String -> prefs.getString(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
        emit(value)
    }

    override fun getKeepBackupHistory(): Flow<Boolean> = preferenceFlow(KEY_KEEP_BACKUP_HISTORY, false)

    override suspend fun setKeepBackupHistory(keep: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_BACKUP_HISTORY, keep).apply()
    }

    override fun isNotificationEnabled(): Flow<Boolean> = preferenceFlow(KEY_NOTIFICATIONS_ENABLED, false)

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override fun getNotificationTime(): Flow<Pair<Int, Int>> {
        return callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                if (key == KEY_NOTIFICATION_HOUR || key == KEY_NOTIFICATION_MINUTE) {
                    trySend(Pair(p.getInt(KEY_NOTIFICATION_HOUR, 9), p.getInt(KEY_NOTIFICATION_MINUTE, 0)))
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            trySend(Pair(prefs.getInt(KEY_NOTIFICATION_HOUR, 9), prefs.getInt(KEY_NOTIFICATION_MINUTE, 0)))
            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
    }

    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_HOUR, hour)
            .putInt(KEY_NOTIFICATION_MINUTE, minute)
            .apply()
    }

    override fun isAutoScrollEnabled(): Flow<Boolean> = preferenceFlow(KEY_AUTO_SCROLL_ENABLED, true)

    override suspend fun setAutoScrollEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SCROLL_ENABLED, enabled).apply()
    }

    override fun getGeminiApiKey(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_GEMINI_API_KEY) {
                trySend(p.getString(key, null))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(KEY_GEMINI_API_KEY, null))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setGeminiApiKey(key: String?) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key).apply()
    }

    override fun getGoogleAccountName(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_GOOGLE_ACCOUNT_NAME) {
                trySend(p.getString(key, null))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(KEY_GOOGLE_ACCOUNT_NAME, null))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setGoogleAccountName(name: String?) {
        prefs.edit().putString(KEY_GOOGLE_ACCOUNT_NAME, name).apply()
    }

    override fun isAutoSyncEnabled(): Flow<Boolean> = preferenceFlow(KEY_AUTO_SYNC_ENABLED, false)

    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply()
    }

    override fun isAutoSpeakEnabled(): Flow<Boolean> = preferenceFlow(KEY_AUTO_SPEAK_ENABLED, false)

    override suspend fun setAutoSpeakEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK_ENABLED, enabled).apply()
    }

    override fun getAppTheme(): Flow<com.eliasgreen18.vocabularytracker.domain.model.AppTheme> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_APP_THEME) {
                val themeName = p.getString(key, com.eliasgreen18.vocabularytracker.domain.model.AppTheme.SYSTEM.name)
                trySend(com.eliasgreen18.vocabularytracker.domain.model.AppTheme.valueOf(themeName!!))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        val current = prefs.getString(KEY_APP_THEME, com.eliasgreen18.vocabularytracker.domain.model.AppTheme.SYSTEM.name)
        trySend(com.eliasgreen18.vocabularytracker.domain.model.AppTheme.valueOf(current!!))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setAppTheme(theme: com.eliasgreen18.vocabularytracker.domain.model.AppTheme) {
        prefs.edit().putString(KEY_APP_THEME, theme.name).apply()
    }

    override fun getUserName(): Flow<String> = preferenceFlow(KEY_USER_NAME, "Reader")
    
    override suspend fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun getReaderTheme(): Flow<com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_READER_THEME) {
                val themeName = p.getString(key, com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme.PAPER.name)
                trySend(com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme.valueOf(themeName!!))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        val current = prefs.getString(KEY_READER_THEME, com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme.PAPER.name)
        trySend(com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme.valueOf(current!!))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setReaderTheme(theme: com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme) {
        prefs.edit().putString(KEY_READER_THEME, theme.name).apply()
    }

    override fun getReaderFontSize(): Flow<Int> = preferenceFlow(KEY_READER_FONT_SIZE, 18)

    override suspend fun setReaderFontSize(size: Int) {
        prefs.edit().putInt(KEY_READER_FONT_SIZE, size).apply()
    }
}
