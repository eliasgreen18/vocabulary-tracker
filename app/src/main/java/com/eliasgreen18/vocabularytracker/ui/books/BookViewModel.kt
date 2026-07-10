package com.eliasgreen18.vocabularytracker.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.BookWithStats
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    getBooksUseCase: GetBooksUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val getChapterByNumberUseCase: GetChapterByNumberUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase,
    private val repository: BookRepository
) : ViewModel() {

    val booksState: StateFlow<List<BookWithStats>> = getBooksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBook(title: String, author: String, language: String, genre: String? = null) {
        viewModelScope.launch {
            val newBook = Book(
                title = title,
                author = author,
                language = language,
                genre = genre
            )
            addBookUseCase(newBook)
        }
    }

    fun onBookClicked(
        bookId: Long, 
        onSessionReady: (Long) -> Unit, 
        onAskChapterNumber: (Long) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateLastOpened(bookId)
            val activeSession = getActiveSessionUseCase(bookId).first()
            if (activeSession != null) {
                onSessionReady(activeSession.id)
            } else {
                onAskChapterNumber(bookId)
            }
        }
    }

    fun onChapterNumberEntered(bookId: Long, number: String, onExists: (Long) -> Unit, onNew: (String) -> Unit) {
        viewModelScope.launch {
            val existingChapter = getChapterByNumberUseCase(bookId, number)
            if (existingChapter != null) {
                val sessionId = startReadingSessionUseCase(existingChapter.id)
                onExists(sessionId)
            } else {
                onNew(number)
            }
        }
    }

    fun startSessionWithNewChapter(bookId: Long, number: String, title: String?, onSessionStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val chapterId = upsertChapterUseCase(
                Chapter(bookId = bookId, number = number, title = title)
            )
            val sessionId = startReadingSessionUseCase(chapterId)
            onSessionStarted(sessionId)
        }
    }
}
