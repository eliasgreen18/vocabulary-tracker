package com.eliasgreen18.vocabularytracker.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ChapterStats
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.usecase.GetChapterStatsUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.GetSessionOccurrencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getChapterStatsUseCase: GetChapterStatsUseCase,
    getSessionOccurrencesUseCase: GetSessionOccurrencesUseCase
) : ViewModel() {

    private val chapterId: Long = checkNotNull(savedStateHandle["chapterId"])

    val stats: StateFlow<ChapterStats?> = getChapterStatsUseCase(chapterId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val words: StateFlow<List<WordWithCount>> = getSessionOccurrencesUseCase(chapterId) // Using chapterId as sessionId works because mapping is consistent
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
