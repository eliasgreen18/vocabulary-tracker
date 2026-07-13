package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class StatsUiState(
    val globalStats: GlobalStats? = null,
    val weeklyStats: WeeklyReadingStats? = null,
    val dueCount: Int = 0,
    val heatmap: ActivityHeatmap? = null,
    val analytics: ReadingAnalytics? = null,
    val mastery: MasteryAnalytics? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    getGlobalStatsUseCase: GetGlobalStatsUseCase,
    getWeeklyReadingStatsUseCase: GetWeeklyReadingStatsUseCase,
    getDueWordsUseCase: GetDueWordsUseCase,
    getActivityHeatmapUseCase: GetActivityHeatmapUseCase,
    getReadingAnalyticsUseCase: GetReadingAnalyticsUseCase,
    getMasteryAnalyticsUseCase: GetMasteryAnalyticsUseCase
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        combine(getGlobalStatsUseCase(), getWeeklyReadingStatsUseCase(), getDueWordsUseCase()) { g, w, d -> Triple(g, w, d) },
        combine(getActivityHeatmapUseCase(), getReadingAnalyticsUseCase(), getMasteryAnalyticsUseCase()) { h, a, m -> Triple(h, a, m) }
    ) { part1, part2 ->
        val (global, weekly, due) = part1
        val (heatmap, analytics, mastery) = part2

        StatsUiState(
            globalStats = global,
            weeklyStats = weekly,
            dueCount = due.size,
            heatmap = heatmap,
            analytics = analytics,
            mastery = mastery,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}
