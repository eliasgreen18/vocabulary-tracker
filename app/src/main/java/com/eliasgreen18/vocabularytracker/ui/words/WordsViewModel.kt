package com.eliasgreen18.vocabularytracker.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.usecase.GetWordsByMasteryUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.SearchWordsUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.ToggleFocusWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val searchWordsUseCase: SearchWordsUseCase,
    private val getWordsByMasteryUseCase: GetWordsByMasteryUseCase,
    private val toggleFocusWordUseCase: ToggleFocusWordUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _masteryFilter = MutableStateFlow<WordMastery?>(null)
    val masteryFilter = _masteryFilter.asStateFlow()

    val searchResults: StateFlow<List<WordWithCount>> = combine(
        _searchQuery.debounce(300),
        _masteryFilter
    ) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        searchWordsUseCase(query).map { words ->
            getWordsByMasteryUseCase(words, filter)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: WordMastery?) {
        _masteryFilter.value = filter
    }

    fun onToggleFocus(wordId: Long, isFocus: Boolean) {
        viewModelScope.launch {
            toggleFocusWordUseCase(wordId, isFocus)
        }
    }
}
