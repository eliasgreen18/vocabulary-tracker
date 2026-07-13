package com.eliasgreen18.vocabularytracker.domain.repository

import com.eliasgreen18.vocabularytracker.domain.model.AppTheme
import com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getKeepBackupHistory(): Flow<Boolean>
    suspend fun setKeepBackupHistory(keep: Boolean)

    fun isNotificationEnabled(): Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)

    fun getNotificationTime(): Flow<Pair<Int, Int>>
    suspend fun setNotificationTime(hour: Int, minute: Int)

    fun isAutoScrollEnabled(): Flow<Boolean>
    suspend fun setAutoScrollEnabled(enabled: Boolean)

    fun getAppTheme(): Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)

    fun getGeminiApiKey(): Flow<String?>
    suspend fun setGeminiApiKey(key: String?)

    fun getGoogleAccountName(): Flow<String?>
    suspend fun setGoogleAccountName(name: String?)

    fun isAutoSyncEnabled(): Flow<Boolean>
    suspend fun setAutoSyncEnabled(enabled: Boolean)

    fun isAutoSpeakEnabled(): Flow<Boolean>
    suspend fun setAutoSpeakEnabled(enabled: Boolean)

    fun getUserName(): Flow<String>
    suspend fun setUserName(name: String)

    fun getReaderTheme(): Flow<ReaderTheme>
    suspend fun setReaderTheme(theme: ReaderTheme)

    fun getReaderFontSize(): Flow<Int>
    suspend fun setReaderFontSize(size: Int)
}
