package com.eliasgreen18.vocabularytracker.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.local.dao.HighlightDao
import com.eliasgreen18.vocabularytracker.data.local.entity.HighlightEntity
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

data class EpubContent(
    val title: String,
    val chapters: List<EpubChapter>
)

data class EpubChapter(
    val title: String,
    val plainText: String,
    val originalIndex: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EpubReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val registerWordUseCase: RegisterWordUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val getChapterByNumberUseCase: GetChapterByNumberUseCase,
    private val upsertChapterUseCase: UpsertChapterUseCase,
    private val highlightDao: HighlightDao
) : ViewModel() {

    val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _epubContent = MutableStateFlow<EpubContent?>(null)
    val epubContent: StateFlow<EpubContent?> = _epubContent.asStateFlow()

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()
    
    private val _initialScrollOffset = MutableStateFlow(0)
    val initialScrollOffset: StateFlow<Int> = _initialScrollOffset.asStateFlow()

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
        loadEpub()
    }

    private fun loadEpub() {
        viewModelScope.launch {
            val bookData = bookRepository.getBookById(bookId).first()
            currentBook = bookData
            val path = bookData?.filePath ?: return@launch
            val file = File(path)
            if (!file.exists()) return@launch
            
            // Restore last position from database
            _currentChapterIndex.value = bookData.lastChapterIndex
            _initialScrollOffset.value = bookData.lastScrollOffset

            val content = parseEpubRobustly(file, bookData.title)
            if (content != null) {
                _epubContent.value = content
                syncSessions()
            }
        }
    }

    private suspend fun syncSessions() {
        val index = _currentChapterIndex.value
        val chapters = _epubContent.value?.chapters ?: return
        val currentEpubChapter = chapters.getOrNull(index) ?: return

        // Robust Parsing for Chapter Number and Title
        val fullTitle = currentEpubChapter.title
        var chapterNumber = (index + 1).toString()
        var cleanTitle = fullTitle

        // Try to find a number at the start or after "Chapter" keyword
        val complexRegex = Regex("(?i)(?:chapter\\s*)?(\\d+)[\\s.:]*(.*)")
        val match = complexRegex.find(fullTitle)
        
        if (match != null) {
            chapterNumber = match.groupValues[1]
            val suffix = match.groupValues[2].trim()
            // If the suffix is empty, it means the title was just "Chapter 1" or "1"
            // In that case, we keep cleanTitle as null/empty to let displayTitle handle it
            cleanTitle = if (suffix.isNotEmpty()) suffix else ""
        } else if (fullTitle.startsWith("Prologue", ignoreCase = true)) {
            chapterNumber = "0"
            cleanTitle = "Prologue"
        }

        val existing = getChapterByNumberUseCase(bookId, chapterNumber)
        val chapterId = existing?.id ?: upsertChapterUseCase(
            Chapter(bookId = bookId, number = chapterNumber, title = cleanTitle.ifBlank { null })
        )
        
        activeSessionId = startReadingSessionUseCase(chapterId)
    }

    private suspend fun parseEpubRobustly(file: File, fallbackTitle: String): EpubContent? = withContext(Dispatchers.IO) {
        try {
            val zipFile = ZipFile(file)
            val containerEntry = zipFile.getEntry("META-INF/container.xml") ?: return@withContext null
            val containerXml = zipFile.getInputStream(containerEntry).bufferedReader().readText()
            val opfPath = Regex("full-path=\"([^\"]+)\"").find(containerXml)?.groupValues?.get(1) ?: return@withContext null
            
            val opfEntry = zipFile.getEntry(opfPath) ?: return@withContext null
            val opfXml = zipFile.getInputStream(opfEntry).bufferedReader().readText()
            val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
            
            val manifestMap = mutableMapOf<String, String>()
            Regex("<item [^>]*id=\"([^\"]+)\" [^>]*href=\"([^\"]+)\"").findAll(opfXml).forEach { 
                manifestMap[it.groupValues[1]] = it.groupValues[2]
            }
            
            val spineOrder = Regex("<itemref [^>]*idref=\"([^\"]+)\"").findAll(opfXml).map { it.groupValues[1] }.toList()
            
            val chapters = mutableListOf<EpubChapter>()
            // Expanded ignore list for metadata sections
            val ignoreKeywords = listOf(
                "author", "content", "title page", "copyright", "dedication", 
                "introduction", "about the author", "contents", "table of contents",
                "preface", "foreword", "acknowledgments"
            )

            spineOrder.forEachIndexed { index, idref ->
                val href = manifestMap[idref] ?: return@forEachIndexed
                val entryPath = opfDir + href
                val entry = zipFile.getEntry(entryPath) ?: return@forEachIndexed
                
                zipFile.getInputStream(entry).use { stream ->
                    val html = stream.bufferedReader().readText()
                    val doc = Jsoup.parse(html)
                    val bodyText = doc.body().text()
                    val title = doc.title().ifBlank { "Section ${index + 1}" }
                    
                    // Filter: real chapters usually have substantial text and aren't boilerplate
                    val isMeaningful = bodyText.length > 300 
                    val isMetaSection = ignoreKeywords.any { title.contains(it, ignoreCase = true) }
                    
                    if (isMeaningful && !isMetaSection) {
                        chapters.add(EpubChapter(
                            title = title,
                            plainText = bodyText,
                            originalIndex = index
                        ))
                    }
                }
            }
            
            zipFile.close()
            if (chapters.isEmpty()) return@withContext null
            
            EpubContent(title = fallbackTitle, chapters = chapters)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun onChapterSelected(index: Int) {
        if (_currentChapterIndex.value == index) return
        _currentChapterIndex.value = index
        _initialScrollOffset.value = 0 // Reset scroll for NEW chapter
        savePosition(0) // Persistent reset
        viewModelScope.launch {
            syncSessions()
        }
    }
    
    fun onScrollPositionChanged(offset: Int) {
        // Debounce or only save meaningful changes to avoid excessive DB writes
        // We'll save the current offset
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
        viewModelScope.launch {
            registerWordUseCase(sessionId = sessionId, text = text, snippet = contextSnippet)
        }
    }

    fun addHighlight(start: Int, end: Int, text: String, colorHex: String = "#FFFF00") {
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

    fun removeHighlightsInRange(start: Int, end: Int) {
        viewModelScope.launch {
            highlightDao.deleteHighlightsInRange(bookId, _currentChapterIndex.value, start, end)
        }
    }
}
