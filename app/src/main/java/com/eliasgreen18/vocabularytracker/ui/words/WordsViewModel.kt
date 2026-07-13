package com.eliasgreen18.vocabularytracker.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.BookWithStats
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class FilterState(
    val mastery: WordMastery? = null,
    val bookId: Long? = null,
    val author: String? = null,
    val isFavoriteOnly: Boolean = false,
    val minHits: Int? = null,
    val maxHits: Int? = null,
)

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val searchWordsUseCase: SearchWordsUseCase,
    private val getWordsByMasteryUseCase: GetWordsByMasteryUseCase,
    private val toggleFocusWordUseCase: ToggleFocusWordUseCase,
    private val batchDeleteWordsUseCase: BatchDeleteWordsUseCase,
    private val batchToggleFocusUseCase: BatchToggleFocusUseCase,
    getBooksUseCase: GetBooksUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(FilterState())
    val filters = _filters.asStateFlow()

    private val _selectedWordIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedWordIds = _selectedWordIds.asStateFlow()

    val availableBooks: StateFlow<List<BookWithStats>> = getBooksUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val availableAuthors: StateFlow<List<String>> = wordRepository.getAllAuthors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSelectionMode = _selectedWordIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val searchResults: StateFlow<List<WordWithCount>> = combine(
        _searchQuery.debounce(300.milliseconds),
        _filters
    ) { query, f ->
        query to f
    }.flatMapLatest { (query, f) ->
        searchWordsUseCase(
            query = query,
            bookId = f.bookId,
            author = f.author,
            isFavorite = if (f.isFavoriteOnly) true else null,
            minHits = f.minHits,
            maxHits = f.maxHits
        ).map { words ->
            getWordsByMasteryUseCase(words, f.mastery)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun updateFilters(newFilters: FilterState) {
        _filters.value = newFilters
    }
    
    fun clearFilters() {
        _filters.value = FilterState()
    }

    fun onToggleFocus(wordId: Long, isFocus: Boolean) {
        viewModelScope.launch {
            toggleFocusWordUseCase(wordId, isFocus)
        }
    }

    fun toggleSelection(wordId: Long) {
        _selectedWordIds.update { current ->
            if (current.contains(wordId)) current - wordId
            else current + wordId
        }
    }

    fun clearSelection() {
        _selectedWordIds.value = emptySet()
    }

    fun deleteSelectedWords() {
        val ids = _selectedWordIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            batchDeleteWordsUseCase(ids)
            clearSelection()
        }
    }

    fun toggleFocusForSelected(isFocus: Boolean) {
        val ids = _selectedWordIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            batchToggleFocusUseCase(ids, isFocus)
            clearSelection()
        }
    }
}
