package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReadingProfile
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetReadingProfileUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val getGlobalStatsUseCase: GetGlobalStatsUseCase
) {
    operator fun invoke(): Flow<ReadingProfile> {
        return combine(
            getGlobalStatsUseCase(),
            wordRepository.getBookContributions(),
            wordRepository.getAuthorStats()
        ) { stats, bookStats, authorStats ->
            val efficiency = if (stats.uniqueWordsCount > 0) {
                (stats.learnedWordsCount * 100) / stats.uniqueWordsCount
            } else 0

            ReadingProfile(
                topBookTeacher = bookStats.firstOrNull()?.bookTitle,
                topAuthorTeacher = authorStats.firstOrNull()?.author,
                avgDaysToMaster = 14, // Simple placeholder
                totalChaptersRead = stats.totalChaptersCount,
                learningEfficiency = efficiency,
                vocabOrigins = bookStats
            )
        }
    }
}
