package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getGlobalStatsUseCase: GetGlobalStatsUseCase,
    getWeeklyReadingStatsUseCase: GetWeeklyReadingStatsUseCase,
    getDueWordsUseCase: GetDueWordsUseCase,
    getActivityHeatmapUseCase: GetActivityHeatmapUseCase,
    getReadingAnalyticsUseCase: GetReadingAnalyticsUseCase,
    getMasteryAnalyticsUseCase: GetMasteryAnalyticsUseCase
) : ViewModel() {

    val globalStats: StateFlow<GlobalStats?> = getGlobalStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weeklyStats: StateFlow<WeeklyReadingStats?> = getWeeklyReadingStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dueCount: StateFlow<Int> = getDueWordsUseCase()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val heatmapState: StateFlow<ActivityHeatmap?> = getActivityHeatmapUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val analyticsState: StateFlow<ReadingAnalytics?> = getReadingAnalyticsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val masteryState: StateFlow<MasteryAnalytics?> = getMasteryAnalyticsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
