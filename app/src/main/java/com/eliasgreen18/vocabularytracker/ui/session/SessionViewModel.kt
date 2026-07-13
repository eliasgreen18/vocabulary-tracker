package com.eliasgreen18.vocabularytracker.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import com.eliasgreen18.vocabularytracker.util.VTLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SessionViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    getSessionWithBookUseCase: GetSessionWithBookUseCase,
    private val endReadingSessionUseCase: EndReadingSessionUseCase,
    private val updateSessionDurationUseCase: UpdateSessionDurationUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase,
    private val registerWordUseCase: RegisterWordUseCase,
    private val getWordsByMasteryUseCase: GetWordsByMasteryUseCase,
    private val toggleFocusWordUseCase: ToggleFocusWordUseCase,
    private val deleteLatestSessionOccurrenceUseCase: DeleteLatestSessionOccurrenceUseCase,
    private val renameWordUseCase: RenameWordUseCase,
    preferencesRepository: UserPreferencesRepository,
    getSessionOccurrencesUseCase: GetSessionOccurrencesUseCase
) : ViewModel() {

    private val tag = "SessionVM"
    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    val sessionInfo: StateFlow<ActiveSessionInfo?> = getSessionWithBookUseCase(sessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _rawSessionWords = getSessionOccurrencesUseCase(sessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _masteryFilter = MutableStateFlow<WordMastery?>(null)

    val sessionWords: StateFlow<List<WordWithCount>> = combine(_rawSessionWords, _masteryFilter) { words, filter ->
        getWordsByMasteryUseCase(words, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val autoScrollEnabled: StateFlow<Boolean> = preferencesRepository.isAutoScrollEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Timer Logic: Initialized from DB
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()
    
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()
    
    private var timerJob: Job? = null
    private var isInitialized = false

    init {
        VTLog.d(tag, "Session Initialized: $sessionId")
        
        // 1. Load initial duration from Database
        viewModelScope.launch {
            sessionInfo.filterNotNull().first().let { info ->
                _elapsedSeconds.value = info.session.activeDurationSeconds
                isInitialized = true
                VTLog.d(tag, "Restored timer from DB: ${_elapsedSeconds.value}s")
            }
        }
        
        // 2. CRITICAL: Aggressive Auto-Save every 3 seconds
        viewModelScope.launch {
            while (true) {
                delay(3.seconds)
                if (_isTimerRunning.value) {
                    VTLog.d(tag, "Auto-saving timer to DB: ${_elapsedSeconds.value}s")
                    updateSessionDurationUseCase(sessionId, _elapsedSeconds.value)
                }
            }
        }
    }

    fun resumeTimer() {
        viewModelScope.launch {
            // Wait for DB load to finish before resuming to avoid starting from 0
            while (!isInitialized) {
                delay(50)
            }
            if (_isTimerRunning.value) return@launch
            _isTimerRunning.value = true
            startTimer()
            VTLog.d(tag, "Timer Resumed at: ${_elapsedSeconds.value}s")
        }
    }

    fun pauseTimer() {
        if (!_isTimerRunning.value) return
        _isTimerRunning.value = false
        timerJob?.cancel()
        VTLog.d(tag, "Timer Paused at: ${_elapsedSeconds.value}s. Performing emergency save...")
        
        // Immediate save to DB on pause
        viewModelScope.launch(Dispatchers.IO) {
            updateSessionDurationUseCase(sessionId, _elapsedSeconds.value)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isTimerRunning.value) {
                    _elapsedSeconds.value++
                }
            }
        }
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) pauseTimer() else resumeTimer()
    }

    fun endSession(onSessionEnded: () -> Unit) {
        viewModelScope.launch {
            // Save final time before ending
            endReadingSessionUseCase(sessionId, _elapsedSeconds.value)
            onSessionEnded()
        }
    }

    fun updateChapterInfo(number: String, title: String?) {
        viewModelScope.launch {
            sessionInfo.value?.chapter?.let { chapter ->
                val updatedChapter = chapter.copy(number = number, title = title)
                upsertChapterUseCase(updatedChapter)
            }
        }
    }

    fun recordWord(text: String, snippet: String? = null) {
        viewModelScope.launch {
            registerWordUseCase(sessionId, text.trim().lowercase(), snippet)
        }
    }

    fun toggleFocus(wordId: Long, isFocus: Boolean) {
        viewModelScope.launch {
            toggleFocusWordUseCase(wordId, isFocus)
        }
    }

    fun deleteFromSession(wordId: Long) {
        viewModelScope.launch {
            deleteLatestSessionOccurrenceUseCase(wordId, sessionId)
        }
    }

    fun renameWord(wordId: Long, newText: String) {
        viewModelScope.launch {
            renameWordUseCase(wordId, newText)
        }
    }
}
