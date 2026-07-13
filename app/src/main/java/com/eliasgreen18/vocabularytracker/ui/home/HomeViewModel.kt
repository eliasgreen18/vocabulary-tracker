package com.eliasgreen18.vocabularytracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val lastBook: BookWithStats? = null,
    val dueCount: Int = 0,
    val wordOfTheDay: Word? = null,
    val streak: Int = 0,
    val totalWords: Int = 0,
    val masteredChaptersCount: Int = 0,
    val userName: String = "Reader",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val getDueWordsUseCase: GetDueWordsUseCase,
    private val wordRepository: WordRepository,
    private val getActivityHeatmapUseCase: GetActivityHeatmapUseCase,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _wordOfTheDay = MutableStateFlow<Word?>(null)
    
    val uiState: StateFlow<HomeUiState> = combine(
        combine(getBooksUseCase(), getDueWordsUseCase(), wordRepository.getTotalWordsCount()) { b, d, t -> Triple(b, d, t) },
        combine(wordRepository.getTotalMasteredChaptersCount(), getActivityHeatmapUseCase(), _wordOfTheDay) { m, h, w -> Triple(m, h, w) },
        preferencesRepository.getUserName()
    ) { part1, part2, name ->
        val (books, due, total) = part1
        val (masteredCount, heatmap, word) = part2
        
        HomeUiState(
            lastBook = books.maxByOrNull { it.lastOpenedAt ?: java.time.Instant.MIN },
            dueCount = due.size,
            wordOfTheDay = word,
            streak = heatmap.streakInfo.currentStreak,
            totalWords = total,
            masteredChaptersCount = masteredCount,
            userName = name,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        refreshWordOfTheDay()
    }

    fun refreshWordOfTheDay() {
        viewModelScope.launch {
            _wordOfTheDay.value = wordRepository.getRandomWord()
        }
    }
}
