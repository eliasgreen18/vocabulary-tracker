package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.GlobalStats
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetGlobalStatsUseCase @Inject constructor(
    private val repository: WordRepository,
    private val chapterRepository: ChapterRepository,
    private val bookRepository: BookRepository,
    private val sessionRepository: ReadingSessionRepository
) {
    operator fun invoke(): Flow<GlobalStats> {
        return combine(
            repository.searchWords(""),
            repository.getTotalOccurrencesCount(),
            repository.getTranslatedWordsCount(),
            repository.getTotalReviewsDoneCount(),
            repository.getTotalSuccessfulReviewsCount(),
            repository.getTotalReviewAttemptsCount(),
            chapterRepository.getTotalChaptersCount(),
            bookRepository.getAllBooks(),
            sessionRepository.getTotalReadingTimeSeconds()
        ) { args ->
            val allWords = args[0] as List<*>
            val totalOccurrences = args[1] as Int
            val translatedCount = args[2] as Int
            val reviewsDone = args[3] as Int
            val successfulReviews = args[4] as Int
            val totalAttempts = args[5] as Int
            val totalChapters = args[6] as Int
            val allBooks = args[7] as List<*>
            val totalTime = args[8] as Long

            val groups = allWords.filterIsInstance<com.eliasgreen18.vocabularytracker.domain.model.WordWithCount>()
                .groupBy { it.mastery }
            
            val completedBooks = allBooks.filterIsInstance<com.eliasgreen18.vocabularytracker.domain.model.Book>()
                .count { it.status == com.eliasgreen18.vocabularytracker.domain.model.BookStatus.FINISHED }

            GlobalStats(
                uniqueWordsCount = allWords.size,
                totalOccurrencesCount = totalOccurrences,
                newWordsCount = groups[WordMastery.NEW]?.size ?: 0,
                learningWordsCount = groups[WordMastery.LEARNING]?.size ?: 0,
                learnedWordsCount = groups[WordMastery.LEARNED]?.size ?: 0,
                translatedWordsCount = translatedCount,
                totalChaptersCount = totalChapters,
                completedBooksCount = completedBooks,
                totalReadingTimeSeconds = totalTime,
                totalReviewsDone = reviewsDone,
                successfulReviews = successfulReviews,
                totalReviewAttempts = totalAttempts
            )
        }
    }
}
