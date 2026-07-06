package com.eliasgreen18.vocabularytracker.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.BookStats
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getBookStatsUseCase: GetBookStatsUseCase,
    getChaptersForBookUseCase: GetChaptersForBookUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val getChapterByNumberUseCase: GetChapterByNumberUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    val bookStats: StateFlow<BookStats?> = getBookStatsUseCase(bookId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val chapters: StateFlow<List<Chapter>> = getChaptersForBookUseCase(bookId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onContinueReading(onSessionReady: (Long) -> Unit) {
        viewModelScope.launch {
            val lastChapter = chapters.value.maxByOrNull { it.number }
            
            if (lastChapter != null) {
                val sessionId = startReadingSessionUseCase(lastChapter.id)
                onSessionReady(sessionId)
            } else {
                // If no chapters, start with number 1
                startNewChapter(1, null, onSessionReady)
            }
        }
    }

    fun startSessionForChapter(chapterId: Long, onSessionReady: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = startReadingSessionUseCase(chapterId)
            onSessionReady(sessionId)
        }
    }

    fun startNewChapter(number: Int, title: String?, onSessionReady: (Long) -> Unit) {
        viewModelScope.launch {
            val existing = getChapterByNumberUseCase(bookId, number)
            val chapterId = existing?.id ?: upsertChapterUseCase(
                Chapter(bookId = bookId, number = number, title = title)
            )
            val sessionId = startReadingSessionUseCase(chapterId)
            onSessionReady(sessionId)
        }
    }
}
