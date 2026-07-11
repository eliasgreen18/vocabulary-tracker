package com.eliasgreen18.vocabularytracker.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.SessionSummary
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    getSessionWithBookUseCase: GetSessionWithBookUseCase,
    private val endReadingSessionUseCase: EndReadingSessionUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase,
    private val registerWordUseCase: RegisterWordUseCase,
    private val getSessionSummaryUseCase: GetSessionSummaryUseCase,
    private val getWordsByMasteryUseCase: GetWordsByMasteryUseCase,
    private val getHighlightedWordsUseCase: GetHighlightedWordsUseCase,
    private val toggleFocusWordUseCase: ToggleFocusWordUseCase,
    private val deleteLatestSessionOccurrenceUseCase: DeleteLatestSessionOccurrenceUseCase,
    private val renameWordUseCase: RenameWordUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    getSessionOccurrencesUseCase: GetSessionOccurrencesUseCase
) : ViewModel() {

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
    val masteryFilter = _masteryFilter.asStateFlow()

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

    val highlightedWords: StateFlow<List<WordWithCount>> = _rawSessionWords.map { words ->
        getHighlightedWordsUseCase(words)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sessionSummary: StateFlow<SessionSummary?> = _rawSessionWords.map { words ->
        getSessionSummaryUseCase(words)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Timer Logic
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()
    
    private val _isTimerRunning = MutableStateFlow(true)
    val isTimerRunning = _isTimerRunning.asStateFlow()
    
    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                if (_isTimerRunning.value) {
                    _elapsedSeconds.value++
                }
                delay(1000)
            }
        }
    }

    fun toggleTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
    }

    private var lastRecordedWord: String? = null
    private var lastRecordedTimestamp: Long = 0

    fun onFilterChanged(filter: WordMastery?) {
        _masteryFilter.value = filter
    }

    fun updateChapterInfo(number: String, title: String?) {
        viewModelScope.launch {
            sessionInfo.value?.chapter?.let { chapter ->
                val updatedChapter = chapter.copy(
                    number = number,
                    title = title
                )
                upsertChapterUseCase(updatedChapter)
            }
        }
    }

    fun recordWord(text: String, snippet: String? = null) {
        val normalized = text.trim().lowercase()
        if (normalized.isBlank()) return

        val now = System.currentTimeMillis()
        // Prevent duplicate spam within 1 second for the SAME word, UNLESS a snippet is provided (which makes it unique context)
        if (snippet == null && normalized == lastRecordedWord && (now - lastRecordedTimestamp) < 1000) {
            return
        }

        lastRecordedWord = normalized
        lastRecordedTimestamp = now

        viewModelScope.launch {
            registerWordUseCase(sessionId, normalized, snippet)
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

    fun endSession(onSessionEnded: () -> Unit) {
        viewModelScope.launch {
            endReadingSessionUseCase(sessionId, _elapsedSeconds.value)
            onSessionEnded()
        }
    }
}
