package com.eliasgreen18.vocabularytracker.ui.words

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.data.util.SpeechService
import com.eliasgreen18.vocabularytracker.domain.model.RelationshipType
import com.eliasgreen18.vocabularytracker.domain.model.WordDetailUiState
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    getWordDetailUseCase: GetWordDetailUseCase,
    private val toggleFocusWordUseCase: ToggleFocusWordUseCase,
    private val retryTranslationUseCase: RetryTranslationUseCase,
    private val updateWordMetadataUseCase: UpdateWordMetadataUseCase,
    private val addWordRelationshipUseCase: AddWordRelationshipUseCase,
    private val deleteWordRelationshipUseCase: DeleteWordRelationshipUseCase,
    private val searchWordsUseCase: SearchWordsUseCase,
    private val getAiInsightsUseCase: GetAiInsightsUseCase,
    private val speechService: SpeechService
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<WordWithCount>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length < 2) flowOf(emptyList())
            else searchWordsUseCase(query).map { list -> 
                list.filter { it.wordId != wordId } 
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<WordDetailUiState?> = getWordDetailUseCase(wordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<WordDetailUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun toggleFocus(isFocus: Boolean) {
        viewModelScope.launch {
            toggleFocusWordUseCase(wordId, isFocus)
        }
    }

    fun retryTranslation() {
        viewModelScope.launch {
            uiState.value?.word?.let { retryTranslationUseCase(it) }
        }
    }

    fun saveManualTranslation(translation: String) {
        viewModelScope.launch {
            updateWordMetadataUseCase.updateTranslation(wordId, translation)
        }
    }

    fun saveManualIpa(ipa: String) {
        viewModelScope.launch {
            updateWordMetadataUseCase.updateIpa(wordId, ipa)
        }
    }

    fun saveManualNotes(notes: String) {
        viewModelScope.launch {
            updateWordMetadataUseCase.updateNotes(wordId, notes)
        }
    }

    fun updateWordText(newText: String) {
        viewModelScope.launch {
            wordRepository.updateWordText(wordId, newText.trim().lowercase())
        }
    }

    fun deleteWord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            wordRepository.deleteWord(wordId)
            onDeleted()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addRelationship(relatedId: Long, type: RelationshipType) {
        viewModelScope.launch {
            addWordRelationshipUseCase(wordId, relatedId, type)
            _searchQuery.value = ""
        }
    }

    fun deleteRelationship(relatedId: Long, type: RelationshipType) {
        viewModelScope.launch {
            deleteWordRelationshipUseCase(wordId, relatedId, type)
        }
    }

    fun generateAiInsights() {
        if (_isAiLoading.value) return
        
        viewModelScope.launch {
            _isAiLoading.value = true
            getAiInsightsUseCase(wordId).fold(
                onSuccess = {
                    _uiEvent.emit(WordDetailUiEvent.InsightsGenerated)
                },
                onFailure = {
                    _uiEvent.emit(WordDetailUiEvent.ShowError(it.message ?: "AI Generation failed"))
                }
            )
            _isAiLoading.value = false
        }
    }

    fun speak(text: String) {
        // Find language from book if possible, otherwise default to "en"
        val lang = uiState.value?.mainLanguage ?: "en"
        speechService.speak(text, lang)
    }
}
