package com.eliasgreen18.vocabularytracker.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ReviewWord
import com.eliasgreen18.vocabularytracker.domain.usecase.GetDueWordsUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.UpdateSrsProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    getDueWordsUseCase: GetDueWordsUseCase,
    private val updateSrsProgressUseCase: UpdateSrsProgressUseCase
) : ViewModel() {

    private val _reviewWords = getDueWordsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _lastReviewFeedback = MutableStateFlow<String?>(null)
    val lastReviewFeedback = _lastReviewFeedback.asStateFlow()

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
            updateSrsProgressUseCase(word.wordId, true)
            provideFeedback(true, word.currentIntervalDays)
            nextWord()
        }
    }

    fun onForgotten() {
        val word = currentWord.value ?: return
        viewModelScope.launch {
            updateSrsProgressUseCase(word.wordId, false)
            provideFeedback(false, word.currentIntervalDays)
            nextWord()
        }
    }

    private fun provideFeedback(remembered: Boolean, lastInterval: Int) {
        val nextInterval = if (remembered) {
            when (lastInterval) {
                0 -> 1
                1 -> 3
                3 -> 7
                7 -> 14
                14 -> 28
                else -> lastInterval * 2
            }
        } else 1
        
        _lastReviewFeedback.value = "Next review: in $nextInterval day${if (nextInterval > 1) "s" else ""}"
    }

    private fun nextWord() {
        _currentIndex.value++
    }
}
