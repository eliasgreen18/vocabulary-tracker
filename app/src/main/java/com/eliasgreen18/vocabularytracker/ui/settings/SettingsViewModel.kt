package com.eliasgreen18.vocabularytracker.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.ImportService
import com.eliasgreen18.vocabularytracker.data.util.NotificationScheduler
import com.eliasgreen18.vocabularytracker.domain.model.AppTheme
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.BatchImportWordsUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.RequestTranslationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler,
    private val importService: ImportService,
    private val batchImportWordsUseCase: BatchImportWordsUseCase,
    private val wordRepository: WordRepository,
    private val requestTranslationUseCase: RequestTranslationUseCase
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

    val autoSpeakEnabled: StateFlow<Boolean> = preferencesRepository.isAutoSpeakEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val appTheme: StateFlow<AppTheme> = preferencesRepository.getAppTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
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

    fun setAutoSpeakEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSpeakEnabled(enabled)
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setAppTheme(theme)
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

    fun triggerMassTranslation() {
        viewModelScope.launch {
            _importStatus.value = "Enqueuing translations..."
            wordRepository.getAllWords().first().forEach { word ->
                if (word.translationStatus == TranslationStatus.NOT_REQUESTED || word.translationStatus == TranslationStatus.ERROR) {
                    requestTranslationUseCase(word)
                }
            }
            _importStatus.value = "Translation tasks added to queue."
        }
    }
    
    fun sendTestNotification() {
        notificationScheduler.scheduleDailyNotificationIn(5) // 5 seconds
    }
}
