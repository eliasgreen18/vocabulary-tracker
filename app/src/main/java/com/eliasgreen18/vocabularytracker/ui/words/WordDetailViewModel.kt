package com.eliasgreen18.vocabularytracker.ui.words

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.WordDetailUiState
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
    private val updateWordMetadataUseCase: UpdateWordMetadataUseCase
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    val uiState: StateFlow<WordDetailUiState?> = getWordDetailUseCase(wordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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
}
