package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.GlobalStats
import com.eliasgreen18.vocabularytracker.domain.model.HomeDashboard
import com.eliasgreen18.vocabularytracker.domain.model.LearningInsight
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
    getLearningInsightsUseCase: GetLearningInsightsUseCase,
    getDueWordsUseCase: GetDueWordsUseCase
) : ViewModel() {

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

    val insights: StateFlow<List<LearningInsight>> = getLearningInsightsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dueCount: StateFlow<Int> = getDueWordsUseCase()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
