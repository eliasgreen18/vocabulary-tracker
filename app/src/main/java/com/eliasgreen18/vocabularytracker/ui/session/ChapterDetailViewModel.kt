package com.eliasgreen18.vocabularytracker.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ChapterStats
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.GetChapterStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getChapterStatsUseCase: GetChapterStatsUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val chapterId: Long = checkNotNull(savedStateHandle["chapterId"])

    val stats: StateFlow<ChapterStats?> = getChapterStatsUseCase(chapterId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val words: StateFlow<List<WordWithCount>> = wordRepository.getChapterWords(chapterId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
