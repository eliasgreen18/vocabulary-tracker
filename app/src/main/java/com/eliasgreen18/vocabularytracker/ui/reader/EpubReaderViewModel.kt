package com.eliasgreen18.vocabularytracker.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.local.dao.HighlightDao
import com.eliasgreen18.vocabularytracker.data.local.entity.HighlightEntity
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import com.eliasgreen18.vocabularytracker.util.VTLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class EpubContent(
    val title: String,
    val chapters: List<EpubChapter>,
)

data class EpubChapter(
    val title: String,
    val plainText: String,
    val originalIndex: Int
)

data class EpubReaderUiState(
    val content: EpubContent? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val readerTheme: ReaderTheme = ReaderTheme.PAPER,
    val fontSize: Int = 18
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EpubReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val registerWordUseCase: RegisterWordUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val getChapterByNumberUseCase: GetChapterByNumberUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase,
    private val parseEpubUseCase: ParseEpubUseCase,
    private val highlightDao: HighlightDao
) : ViewModel() {

    private val tag = "EpubReaderVM"
    val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _epubContent = MutableStateFlow<EpubContent?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()
    
    private val _initialScrollOffset = MutableStateFlow(0)
    val initialScrollOffset: StateFlow<Int> = _initialScrollOffset.asStateFlow()

    val uiState: StateFlow<EpubReaderUiState> = combine(
        _epubContent,
        _isLoading,
        _error,
        preferencesRepository.getReaderTheme(),
        preferencesRepository.getReaderFontSize()
    ) { content, loading, error, theme, fontSize ->
        EpubReaderUiState(content, loading, error, theme, fontSize)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EpubReaderUiState())

    val currentHighlights: StateFlow<List<HighlightEntity>> = combine(
        _currentChapterIndex,
        _epubContent
    ) { index, content -> index to content }
        .flatMapLatest { (index, content) ->
            if (content == null) flowOf(emptyList())
            else highlightDao.getHighlightsForChapter(bookId, index)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeSessionId: Long? = null
    private var currentBook: Book? = null

    init {
        VTLog.d(tag, "Initializing Reader for BookId: $bookId")
        loadEpub()
    }

    private fun loadEpub() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val bookData = bookRepository.getBookById(bookId).first()
                if (bookData == null) {
                    VTLog.e(tag, "Book data not found in DB")
                    _error.value = "Book not found."
                    _isLoading.value = false
                    return@launch
                }
                
                currentBook = bookData
                val path = bookData.filePath ?: run {
                    VTLog.e(tag, "File path is null for book: ${bookData.title}")
                    _error.value = "Book file not found."
                    _isLoading.value = false
                    return@launch
                }
                
                VTLog.d(tag, "Loading EPUB from: $path")
                _currentChapterIndex.value = bookData.lastChapterIndex
                _initialScrollOffset.value = bookData.lastScrollOffset

                parseEpubUseCase(File(path), bookData.title).onSuccess { content ->
                    val sanitizedChapters = content.chapters.map { chapter ->
                        val sanitizedText = sanitizeChapterText(chapter.title, chapter.plainText)
                        chapter.copy(plainText = sanitizedText)
                    }
                    _epubContent.value = content.copy(chapters = sanitizedChapters)
                    _isLoading.value = false
                    syncSessions()
                }.onFailure { e ->
                    VTLog.e(tag, "Failed to parse EPUB", e)
                    _error.value = e.message ?: "Invalid book file."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                VTLog.e(tag, "Unexpected error in loadEpub", e)
                _error.value = "Error loading book."
                _isLoading.value = false
            }
        }
    }

    private fun sanitizeChapterText(title: String, text: String): String {
        val trimmedTitle = title.trim()
        val trimmedText = text.trim()
        
        if (trimmedText.startsWith(trimmedTitle, ignoreCase = true)) {
            return trimmedText.substring(trimmedTitle.length).trim()
        }
        
        val chapterPattern = Regex("(?i)Chapter\\s+\\d+")
        val match = chapterPattern.find(trimmedTitle)
        if ((match != null) && trimmedText.startsWith(match.value, ignoreCase = true)) {
            val titleWords = trimmedTitle.split("\\s+".toRegex()).asSequence().take(3).joinToString(" ")
            if (trimmedText.startsWith(titleWords, ignoreCase = true)) {
                 return trimmedText.substring(titleWords.length).trim()
            }
        }
        
        return text
    }

    private suspend fun syncSessions() {
        val index = _currentChapterIndex.value
        val content = _epubContent.value ?: return
        val currentEpubChapter = content.chapters.getOrNull(index) ?: return

        try {
            val fullTitle = currentEpubChapter.title
            var chapterNumber = (index + 1).toString()
            var cleanTitle = fullTitle

            val complexRegex = Regex("(?i)(?:chapter\\s*)?(\\d+)[\\s.:]*(.*)")
            val match = complexRegex.find(fullTitle)
            
            if (match != null) {
                chapterNumber = match.groupValues[1]
                val suffix = match.groupValues[2].trim()
                cleanTitle = suffix.ifEmpty { "" }
            } else if (fullTitle.startsWith("Prologue", ignoreCase = true)) {
                chapterNumber = "0"
                cleanTitle = "Prologue"
            }

            val existing = getChapterByNumberUseCase(bookId, chapterNumber)
            val chapterId = existing?.id ?: upsertChapterUseCase(
                Chapter(bookId = bookId, number = chapterNumber, title = cleanTitle.ifBlank { null })
            )
            
            VTLog.d(tag, "Syncing Session for Chapter: $chapterNumber (Id: $chapterId)")
            activeSessionId = startReadingSessionUseCase(chapterId)
        } catch (e: Exception) {
            VTLog.e(tag, "Error in syncSessions", e)
        }
    }

    fun onChapterSelected(index: Int) {
        val content = _epubContent.value ?: return
        if (index < 0 || index >= content.chapters.size) {
            VTLog.e(tag, "Invalid chapter index selected: $index")
            return
        }
        
        VTLog.d(tag, "Chapter Selected: $index")
        _currentChapterIndex.value = index
        _initialScrollOffset.value = 0 
        savePosition(0)
        viewModelScope.launch {
            syncSessions()
        }
    }

    fun navigateToNextChapter() {
        val nextIndex = _currentChapterIndex.value + 1
        val content = _epubContent.value ?: return
        if (nextIndex < content.chapters.size) {
            onChapterSelected(nextIndex)
        }
    }

    fun navigateToPrevChapter() {
        val prevIndex = _currentChapterIndex.value - 1
        if (prevIndex >= 0) {
            onChapterSelected(prevIndex)
        }
    }
    
    fun setReaderTheme(theme: ReaderTheme) {
        viewModelScope.launch {
            preferencesRepository.setReaderTheme(theme)
        }
    }

    fun updateFontSize(delta: Int) {
        viewModelScope.launch {
            val current = uiState.value.fontSize
            val newSize = (current + delta).coerceIn(12, 32)
            preferencesRepository.setReaderFontSize(newSize)
        }
    }

    fun onScrollPositionChanged(offset: Int) {
        savePosition(offset)
    }

    private fun savePosition(offset: Int? = null) {
        viewModelScope.launch {
            val book = currentBook ?: return@launch
            val updatedBook = book.copy(
                lastChapterIndex = _currentChapterIndex.value,
                lastScrollOffset = offset ?: book.lastScrollOffset
            )
            bookRepository.updateBook(updatedBook)
            currentBook = updatedBook
        }
    }

    fun addWordToVocabulary(text: String, contextSnippet: String?) {
        val sessionId = activeSessionId ?: return
        VTLog.d(tag, "Adding word: $text")
        viewModelScope.launch {
            registerWordUseCase(sessionId = sessionId, text = text, snippet = contextSnippet)
        }
    }

    fun addHighlight(start: Int, end: Int, text: String, colorHex: String = "#76C49A") {
        viewModelScope.launch {
            val highlight = HighlightEntity(
                bookId = bookId,
                chapterIndex = _currentChapterIndex.value,
                startOffset = start,
                endOffset = end,
                colorHex = colorHex,
                text = text
            )
            highlightDao.insertHighlight(highlight)
        }
    }

    fun deleteHighlight(id: Long) {
        viewModelScope.launch {
            highlightDao.deleteHighlightById(id)
        }
    }
}
