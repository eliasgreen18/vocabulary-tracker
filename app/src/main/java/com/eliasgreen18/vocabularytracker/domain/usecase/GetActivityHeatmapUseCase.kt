package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ActivityHeatmap
import com.eliasgreen18.vocabularytracker.domain.model.StreakInfo
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetActivityHeatmapUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<ActivityHeatmap> {
        return repository.getDailyActivity().map { activityMap ->
            val streakInfo = calculateStreaks(activityMap)
            ActivityHeatmap(
                dailyActivity = activityMap,
                streakInfo = streakInfo
            )
        }
    }

    private fun calculateStreaks(activityMap: Map<LocalDate, Int>): StreakInfo {
        if (activityMap.isEmpty()) return StreakInfo(0, 0, null)

        val sortedDates = activityMap.keys.sortedDescending()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        val lastActivity = sortedDates.first()
        
        // Current Streak
        var currentStreak = 0
        if (lastActivity == today || lastActivity == yesterday) {
            var checkDate = lastActivity
            while (activityMap.containsKey(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        }

        // Longest Streak
        var longestStreak = 0
        var tempStreak = 0
        val allDates = activityMap.keys.sorted()
        if (allDates.isNotEmpty()) {
            var prevDate: LocalDate? = null
            for (date in allDates) {
                if (prevDate == null || ChronoUnit.DAYS.between(prevDate, date) == 1L) {
                    tempStreak++
                } else {
                    longestStreak = maxOf(longestStreak, tempStreak)
                    tempStreak = 1
                }
                prevDate = date
            }
            longestStreak = maxOf(longestStreak, tempStreak)
        }

        return StreakInfo(currentStreak, longestStreak, lastActivity)
    }
}
