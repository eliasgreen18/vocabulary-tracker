package com.eliasgreen18.vocabularytracker.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ReviewWord
import com.eliasgreen18.vocabularytracker.domain.usecase.GetReviewQueueUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.MarkWordNotRememberedUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.MarkWordReviewedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    getReviewQueueUseCase: GetReviewQueueUseCase,
    private val markWordReviewedUseCase: MarkWordReviewedUseCase,
    private val markWordNotRememberedUseCase: MarkWordNotRememberedUseCase
) : ViewModel() {

    private val _reviewWords = getReviewQueueUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    val currentWord: StateFlow<ReviewWord?> = combine(_reviewWords, _currentIndex) { words, index ->
        if (index < words.size) words[index] else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val totalToReview: StateFlow<Int> = _reviewWords.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun onRemembered() {
        val word = currentWord.value ?: return
        viewModelScope.launch {
            markWordReviewedUseCase(word.wordId)
            nextWord()
        }
    }

    fun onForgotten() {
        val word = currentWord.value ?: return
        viewModelScope.launch {
            markWordNotRememberedUseCase(word.wordId)
            nextWord()
        }
    }

    private fun nextWord() {
        _currentIndex.value++
    }
}
