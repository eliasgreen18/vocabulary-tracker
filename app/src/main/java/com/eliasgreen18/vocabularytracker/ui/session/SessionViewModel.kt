package com.eliasgreen18.vocabularytracker.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.SessionSummary
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
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

    fun recordWord(text: String) {
        val normalized = text.trim().lowercase()
        if (normalized.isBlank()) return

        val now = System.currentTimeMillis()
        if (normalized == lastRecordedWord && (now - lastRecordedTimestamp) < 1000) {
            return
        }

        lastRecordedWord = normalized
        lastRecordedTimestamp = now

        viewModelScope.launch {
            registerWordUseCase(sessionId, normalized)
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
            endReadingSessionUseCase(sessionId)
            onSessionEnded()
        }
    }
}
