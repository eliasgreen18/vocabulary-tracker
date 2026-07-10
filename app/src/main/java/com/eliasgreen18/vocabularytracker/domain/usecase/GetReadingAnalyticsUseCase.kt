package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReadingAnalytics
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetReadingAnalyticsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<ReadingAnalytics> {
        return combine(
            repository.getBookContributions(),
            repository.getChapterDifficulties()
        ) { books, chapters ->
            ReadingAnalytics(
                wordsPerBook = books,
                wordsPerChapter = chapters.take(10), // Limit to top 10 for dashboard
                mostChallengingChapter = chapters.firstOrNull(),
                topContributingBook = books.firstOrNull()
            )
        }
    }
}
