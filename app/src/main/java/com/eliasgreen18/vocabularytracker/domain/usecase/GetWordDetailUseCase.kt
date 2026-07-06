package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.WordDetailUiState
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetWordDetailUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(wordId: Long): Flow<WordDetailUiState?> {
        return combine(
            repository.getWordByIdFlow(wordId),
            repository.getWordHistory(wordId)
        ) { word, history ->
            if (word == null) return@combine null
            
            val failCount = word.reviewCount - word.successfulReviews
            val accuracy = if (word.reviewCount > 0) {
                (word.successfulReviews * 100) / word.reviewCount
            } else 0

            WordDetailUiState(
                word = word,
                totalOccurrences = history.size,
                bookCount = history.map { it.bookTitle }.distinct().size,
                chapterCount = history.map { "${it.bookTitle}_${it.chapterNumber}" }.distinct().size,
                firstSeen = history.lastOrNull()?.createdAt,
                lastSeen = history.firstOrNull()?.createdAt,
                history = history,
                // SRS Stats
                nextReviewDate = word.nextReviewAt,
                currentInterval = word.currentIntervalDays,
                successCount = word.successfulReviews,
                failCount = failCount,
                recallAccuracy = accuracy,
                mainLanguage = history.firstOrNull()?.bookLanguage
            )
        }
    }
}
