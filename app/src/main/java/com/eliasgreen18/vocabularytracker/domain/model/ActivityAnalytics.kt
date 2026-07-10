package com.eliasgreen18.vocabularytracker.domain.model

import java.time.LocalDate

data class ActivityHeatmap(
    val dailyActivity: Map<LocalDate, Int>,
    val streakInfo: StreakInfo
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: LocalDate?
)
