package com.eliasgreen18.vocabularytracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.NotificationScheduler
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler
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

    fun handleGoogleSignIn(accountName: String) {
        // TODO: Store account name and update status
    }
}
