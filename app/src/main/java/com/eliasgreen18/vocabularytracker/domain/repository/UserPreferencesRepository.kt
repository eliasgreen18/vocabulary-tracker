package com.eliasgreen18.vocabularytracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getKeepBackupHistory(): Flow<Boolean>
    suspend fun setKeepBackupHistory(keep: Boolean)

    fun isNotificationEnabled(): Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)

    fun getNotificationTime(): Flow<Pair<Int, Int>> // Pair(hour, minute)
    suspend fun setNotificationTime(hour: Int, minute: Int)

    fun isAutoScrollEnabled(): Flow<Boolean>
    suspend fun setAutoScrollEnabled(enabled: Boolean)

    fun getGeminiApiKey(): Flow<String?>
    suspend fun setGeminiApiKey(key: String?)

    fun getGoogleAccountName(): Flow<String?>
    suspend fun setGoogleAccountName(name: String?)

    fun isAutoSyncEnabled(): Flow<Boolean>
    suspend fun setAutoSyncEnabled(enabled: Boolean)

    fun isAutoSpeakEnabled(): Flow<Boolean>
    suspend fun setAutoSpeakEnabled(enabled: Boolean)

    fun getAppTheme(): Flow<com.eliasgreen18.vocabularytracker.domain.model.AppTheme>
    suspend fun setAppTheme(theme: com.eliasgreen18.vocabularytracker.domain.model.AppTheme)
}
