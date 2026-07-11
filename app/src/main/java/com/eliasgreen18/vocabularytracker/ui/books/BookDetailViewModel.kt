package com.eliasgreen18.vocabularytracker.ui.books

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.FileStorageService
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val stats: BookStats? = null,
    val chapters: List<Chapter> = emptyList(),
    val mastery: Map<Long, ChapterMastery> = emptyMap(),
    val activeSession: ReadingSession? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getBookStatsUseCase: GetBookStatsUseCase,
    getChaptersForBookUseCase: GetChaptersForBookUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val getChapterByNumberUseCase: GetChapterByNumberUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val deleteChapterUseCase: DeleteChapterUseCase,
    private val fileStorageService: FileStorageService,
    private val wordRepository: WordRepository,
    private val repository: BookRepository
) : ViewModel() {

    val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<BookDetailUiState> = combine(
        getBookStatsUseCase(bookId),
        getChaptersForBookUseCase(bookId),
        wordRepository.getChapterMastery(bookId),
        getActiveSessionUseCase(bookId),
        _searchQuery
    ) { stats, chapters, mastery, active, query ->
        val filteredChapters = if (query.isBlank()) chapters 
        else chapters.filter { it.number.contains(query, ignoreCase = true) || (it.title?.contains(query, ignoreCase = true) == true) }
        
        BookDetailUiState(
            stats = stats,
            chapters = filteredChapters,
            mastery = mastery,
            activeSession = active,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookDetailUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onContinueReading(onSessionReady: (Long) -> Unit) {
        viewModelScope.launch {
            val lastChapter = uiState.value.chapters.maxByOrNull { it.number }
            
            if (lastChapter != null) {
                val sessionId = startReadingSessionUseCase(lastChapter.id)
                onSessionReady(sessionId)
            } else {
                startNewChapter("1", null, onSessionReady)
            }
        }
    }

    fun startSessionForChapter(chapterId: Long, onSessionReady: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = startReadingSessionUseCase(chapterId)
            onSessionReady(sessionId)
        }
    }

    fun startNewChapter(number: String, title: String?, onSessionReady: (Long) -> Unit) {
        viewModelScope.launch {
            val existing = getChapterByNumberUseCase(bookId, number)
            val chapterId = existing?.id ?: upsertChapterUseCase(
                Chapter(bookId = bookId, number = number, title = title)
            )
            val sessionId = startReadingSessionUseCase(chapterId)
            onSessionReady(sessionId)
        }
    }

    fun updateBookMetadata(
        title: String,
        author: String,
        language: String,
        genre: String?,
        newCoverUri: Uri?,
        newFileUri: Uri? = null
    ) {
        viewModelScope.launch {
            val currentBook = repository.getBookById(bookId).first() ?: return@launch
            
            var coverPath = currentBook.coverPath
            if (newCoverUri != null) {
                currentBook.coverPath?.let { fileStorageService.deleteCover(it) }
                coverPath = fileStorageService.saveBookCover(newCoverUri).getOrNull()
            }

            var filePath = currentBook.filePath
            if (newFileUri != null) {
                val extension = fileStorageService.getExtensionFromUri(newFileUri)
                filePath = fileStorageService.saveDigitalBook(newFileUri, extension).getOrNull()
            }
            
            val updatedBook = currentBook.copy(
                title = title,
                author = author,
                language = language,
                genre = genre,
                coverPath = coverPath,
                filePath = filePath
            )
            repository.updateBook(updatedBook)
        }
    }

    fun finishBook() {
        viewModelScope.launch {
            repository.updateBookStatus(bookId, BookStatus.FINISHED)
        }
    }

    fun resumeBook() {
        viewModelScope.launch {
            repository.updateBookStatus(bookId, BookStatus.READING)
        }
    }

    fun deleteChapter(chapterId: Long) {
        viewModelScope.launch {
            deleteChapterUseCase(chapterId)
        }
    }
}
