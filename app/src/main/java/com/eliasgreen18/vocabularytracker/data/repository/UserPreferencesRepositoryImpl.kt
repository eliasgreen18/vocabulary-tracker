package com.eliasgreen18.vocabularytracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
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
    }

    private fun <T> preferenceFlow(key: String, defaultValue: T): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, k ->
            if (k == key) {
                @Suppress("UNCHECKED_CAST")
                val value = when (defaultValue) {
                    is Boolean -> p.getBoolean(k, defaultValue) as T
                    is Int -> p.getInt(k, defaultValue) as T
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
        val hourFlow = preferenceFlow(KEY_NOTIFICATION_HOUR, 9)
        val minuteFlow = preferenceFlow(KEY_NOTIFICATION_MINUTE, 0)
        
        // Combining them manually for simplicity in this implementation
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
}
