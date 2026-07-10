package com.eliasgreen18.vocabularytracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.BackupService
import com.eliasgreen18.vocabularytracker.domain.usecase.GetHomeDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getHomeDashboardUseCase: GetHomeDashboardUseCase,
    private val backupService: BackupService
) : ViewModel() {

    val activeSessionId: StateFlow<Long?> = getHomeDashboardUseCase().map { dashboard ->
        dashboard.activeSessions.firstOrNull()?.session?.id
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _backupFile = MutableStateFlow<File?>(null)
    val backupFile = _backupFile.asStateFlow()

    private val _backupError = MutableStateFlow<String?>(null)
    val backupError = _backupError.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            backupService.exportBackup()
                .onSuccess { 
                    _backupFile.value = it
                    _backupError.value = null
                }
                .onFailure { 
                    _backupError.value = it.message ?: "Unknown error during backup"
                }
        }
    }
    
    fun clearBackupState() {
        _backupFile.value = null
        _backupError.value = null
    }
}
