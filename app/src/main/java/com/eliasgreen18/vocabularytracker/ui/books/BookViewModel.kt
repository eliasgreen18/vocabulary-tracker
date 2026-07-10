package com.eliasgreen18.vocabularytracker.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.Book
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

    val booksState: StateFlow<List<Book>> = getBooksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBook(title: String, author: String, language: String) {
        viewModelScope.launch {
            val newBook = Book(
                title = title,
                author = author,
                language = language
            )
            addBookUseCase(newBook)
        }
    }

    fun onBookClicked(
        book: Book, 
        onSessionReady: (Long) -> Unit, 
        onNeedChapterInfo: (Book, Int) -> Unit,
        onAskChapterNumber: (Book) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateLastOpened(book.id)
            val activeSession = getActiveSessionUseCase(book.id).first()
            if (activeSession != null) {
                onSessionReady(activeSession.id)
            } else {
                onAskChapterNumber(book)
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
