package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class WeeklyReadingStats(
    val dailyMinutes: Map<LocalDate, Int>,
    val totalMinutesThisWeek: Int,
    val weeklyGoalMinutes: Int = 300 // 5 hours default
) {
    val completionPercentage: Int
        get() = if (weeklyGoalMinutes > 0) (totalMinutesThisWeek * 100) / weeklyGoalMinutes else 0
}

class GetWeeklyReadingStatsUseCase @Inject constructor(
    private val repository: ReadingSessionRepository
) {
    operator fun invoke(): Flow<WeeklyReadingStats> {
        val lastWeek = Instant.now().minus(7, ChronoUnit.DAYS)
        return repository.getDailyReadingDurations(lastWeek).map { durations ->
            val dailyMinutes = durations.mapValues { (it.value / 60).toInt() }
            val totalMinutes = dailyMinutes.values.sum()
            
            WeeklyReadingStats(
                dailyMinutes = dailyMinutes,
                totalMinutesThisWeek = totalMinutes
            )
        }
    }
}
