package com.eliasgreen18.vocabularytracker.ui.words

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.model.WordOccurrenceDetail
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.GetWordHistoryUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.RetryTranslationUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.SaveManualTranslationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    wordRepository: WordRepository,
    getWordHistoryUseCase: GetWordHistoryUseCase,
    private val retryTranslationUseCase: RetryTranslationUseCase,
    private val saveManualTranslationUseCase: SaveManualTranslationUseCase
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    val word: StateFlow<Word?> = wordRepository.getWordByIdFlow(wordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val history: StateFlow<List<WordOccurrenceDetail>> = getWordHistoryUseCase(wordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun retryTranslation() {
        viewModelScope.launch {
            word.value?.let { retryTranslationUseCase(it) }
        }
    }

    fun saveManualTranslation(translation: String) {
        viewModelScope.launch {
            saveManualTranslationUseCase(wordId, translation)
        }
    }
}
