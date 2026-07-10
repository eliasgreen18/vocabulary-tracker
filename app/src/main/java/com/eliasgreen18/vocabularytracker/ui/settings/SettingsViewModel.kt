package com.eliasgreen18.vocabularytracker.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.ImportService
import com.eliasgreen18.vocabularytracker.data.util.NotificationScheduler
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.BatchImportWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler,
    private val importService: ImportService,
    private val batchImportWordsUseCase: BatchImportWordsUseCase
) : ViewModel() {

    val keepBackupHistory: StateFlow<Boolean> = preferencesRepository.getKeepBackupHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.isNotificationEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val notificationTime: StateFlow<Pair<Int, Int>> = preferencesRepository.getNotificationTime()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Pair(9, 0)
        )

    val autoScrollEnabled: StateFlow<Boolean> = preferencesRepository.isAutoScrollEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val geminiApiKey: StateFlow<String?> = preferencesRepository.getGeminiApiKey()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val googleAccountName: StateFlow<String?> = preferencesRepository.getGoogleAccountName()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val autoSyncEnabled: StateFlow<Boolean> = preferencesRepository.isAutoSyncEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus = _importStatus.asStateFlow()

    fun setKeepBackupHistory(keep: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setKeepBackupHistory(keep)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationEnabled(enabled)
            if (enabled) {
                val time = notificationTime.value
                notificationScheduler.scheduleDailyNotification(time.first, time.second)
            } else {
                notificationScheduler.cancelNotifications()
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setNotificationTime(hour, minute)
            if (notificationsEnabled.value) {
                notificationScheduler.scheduleDailyNotification(hour, minute)
            }
        }
    }

    fun setAutoScrollEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoScrollEnabled(enabled)
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSyncEnabled(enabled)
        }
    }

    fun setGeminiApiKey(key: String?) {
        viewModelScope.launch {
            preferencesRepository.setGeminiApiKey(key)
        }
    }

    fun importData(uri: Uri, extension: String, overwrite: Boolean) {
        viewModelScope.launch {
            _importStatus.value = "Processing..."
            val result = if (extension == "json") {
                importService.importFromJson(uri)
            } else {
                importService.importFromCsv(uri)
            }

            result.onSuccess { words ->
                batchImportWordsUseCase(words, overwrite)
                _importStatus.value = "Import Successful: ${words.size} words processed."
            }.onFailure {
                _importStatus.value = "Import Failed: ${it.message}"
            }
        }
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }

    fun onGoogleAccountConnected(email: String) {
        viewModelScope.launch {
            preferencesRepository.setGoogleAccountName(email)
        }
    }

    fun disconnectGoogleAccount() {
        viewModelScope.launch {
            preferencesRepository.setGoogleAccountName(null)
            preferencesRepository.setAutoSyncEnabled(false)
        }
    }
}
