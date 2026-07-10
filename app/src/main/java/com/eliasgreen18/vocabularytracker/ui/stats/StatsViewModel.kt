package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.GlobalStats
import com.eliasgreen18.vocabularytracker.domain.model.HomeDashboard
import com.eliasgreen18.vocabularytracker.domain.model.LearningProgress
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getHomeDashboardUseCase: GetHomeDashboardUseCase,
    getLearningProgressUseCase: GetLearningProgressUseCase,
    getGlobalStatsUseCase: GetGlobalStatsUseCase,
    getDueWordsUseCase: GetDueWordsUseCase,
    getReadingAnalyticsUseCase: GetReadingAnalyticsUseCase,
    getActivityHeatmapUseCase: GetActivityHeatmapUseCase,
    getMasteryAnalyticsUseCase: GetMasteryAnalyticsUseCase
) : ViewModel() {

    val masteryState: StateFlow<com.eliasgreen18.vocabularytracker.domain.model.MasteryAnalytics?> = getMasteryAnalyticsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val heatmapState: StateFlow<com.eliasgreen18.vocabularytracker.domain.model.ActivityHeatmap?> = getActivityHeatmapUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val analyticsState: StateFlow<com.eliasgreen18.vocabularytracker.domain.model.ReadingAnalytics?> = getReadingAnalyticsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val dashboardState: StateFlow<HomeDashboard?> = getHomeDashboardUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val learningProgress: StateFlow<LearningProgress?> = getLearningProgressUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val globalStats: StateFlow<GlobalStats?> = getGlobalStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val dueCount: StateFlow<Int> = getDueWordsUseCase()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
