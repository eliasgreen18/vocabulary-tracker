package com.eliasgreen18.vocabularytracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.remote.drive.GoogleDriveService
import com.eliasgreen18.vocabularytracker.data.util.BackupService
import com.eliasgreen18.vocabularytracker.data.util.ExportService
import com.eliasgreen18.vocabularytracker.data.util.ProExportService
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.GetExportDataUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.GetHomeDashboardUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.SearchWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getHomeDashboardUseCase: GetHomeDashboardUseCase,
    searchWordsUseCase: SearchWordsUseCase,
    private val backupService: BackupService,
    private val googleDriveService: GoogleDriveService,
    private val exportService: ExportService,
    private val proExportService: ProExportService,
    private val getExportDataUseCase: GetExportDataUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // PRE-LOADER: Triggers vocabulary load on app start to warm up DB cache
    init {
        searchWordsUseCase("").launchIn(viewModelScope)
    }

    val activeSessionId: StateFlow<Long?> = getHomeDashboardUseCase().map { dashboard ->
        dashboard.activeSessions.firstOrNull()?.session?.id
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val appTheme: StateFlow<com.eliasgreen18.vocabularytracker.domain.model.AppTheme> = preferencesRepository.getAppTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.eliasgreen18.vocabularytracker.domain.model.AppTheme.SYSTEM
        )

    private val _backupFile = MutableStateFlow<File?>(null)
    val backupFile = _backupFile.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            backupService.exportBackup()
                .onSuccess { 
                    _backupFile.value = it
                }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            val words = getExportDataUseCase().first()
            exportService.exportToCsv(words)
                .onSuccess { _backupFile.value = it }
        }
    }

    fun exportToJson() {
        viewModelScope.launch {
            val words = getExportDataUseCase().first()
            exportService.exportToJson(words)
                .onSuccess { _backupFile.value = it }
        }
    }

    fun exportToAnki(selectedWords: List<WordWithCount>? = null) {
        viewModelScope.launch {
            val words = selectedWords ?: getExportDataUseCase().first()
            proExportService.exportToAnki(words)
                .onSuccess { _backupFile.value = it }
        }
    }

    fun exportToQuizlet(selectedWords: List<WordWithCount>? = null) {
        viewModelScope.launch {
            val words = selectedWords ?: getExportDataUseCase().first()
            proExportService.exportToQuizlet(words)
                .onSuccess { _backupFile.value = it }
        }
    }

    fun syncToDrive() {
        viewModelScope.launch {
            _syncStatus.value = "Starting Sync..."
            
            val accountName = preferencesRepository.getGoogleAccountName().first()
            if (accountName == null) {
                _syncStatus.value = "Sync Failed: Google Drive not connected"
                return@launch
            }
            
            googleDriveService.setupService(accountName)

            val backupResult = backupService.exportBackup()
            backupResult.onSuccess { file ->
                googleDriveService.uploadDatabase(file)
                    .onSuccess { 
                        _syncStatus.value = "Sync Successful!"
                    }
                    .onFailure { 
                        val errorMsg = it.message ?: it.toString()
                        _syncStatus.value = "Sync Failed: $errorMsg"
                    }
            }.onFailure {
                _syncStatus.value = "Export Failed"
            }
        }
    }
    
    fun clearStatus() {
        _backupFile.value = null
        _syncStatus.value = null
    }

    fun clearBackupState() {
        _backupFile.value = null
    }
}
