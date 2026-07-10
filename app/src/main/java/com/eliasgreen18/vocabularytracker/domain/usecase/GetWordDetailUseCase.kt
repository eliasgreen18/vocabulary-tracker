package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

class GetWordDetailUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(wordId: Long): Flow<WordDetailUiState?> {
        return combine(
            repository.getWordByIdFlow(wordId),
            repository.getWordHistory(wordId),
            repository.getRelatedWords(wordId)
        ) { word, history, related ->
            if (word == null) return@combine null
            
            val failCount = word.reviewCount - word.successfulReviews
            val accuracy = if (word.reviewCount > 0) {
                (word.successfulReviews * 100) / word.reviewCount
            } else 0

            val journey = mutableListOf<JourneyEvent>()
            
            // 1. Sightings from occurrences
            history.reversed().forEachIndexed { index, occurrence ->
                val timestamp = occurrence.createdAt
                if (index == 0) {
                    journey.add(JourneyEvent.Discovery(
                        timestamp = timestamp,
                        bookTitle = occurrence.bookTitle,
                        chapterDisplay = occurrence.displayChapter,
                        snippet = occurrence.snippet
                    ))
                } else {
                    journey.add(JourneyEvent.Encounter(
                        timestamp = timestamp,
                        bookTitle = occurrence.bookTitle,
                        chapterDisplay = occurrence.displayChapter,
                        snippet = occurrence.snippet
                    ))
                }
            }

            // 2. SRS Milestone (Last Review)
            word.lastSrsReviewAt?.let { lastReviewed ->
                journey.add(JourneyEvent.Reviewed(
                    timestamp = lastReviewed,
                    nextInterval = word.currentIntervalDays,
                    successful = true
                ))
            }

            // 3. Mastery Milestone
            if (WordMastery.fromCount(history.size) == WordMastery.LEARNED) {
                history.firstOrNull()?.let {
                    journey.add(JourneyEvent.Mastered(it.createdAt))
                }
            }

            WordDetailUiState(
                word = word,
                totalOccurrences = history.size,
                bookCount = history.map { it.bookTitle }.distinct().size,
                chapterCount = history.map { "${it.bookTitle}_${it.chapterNumber}" }.distinct().size,
                firstSeen = history.lastOrNull()?.createdAt,
                lastSeen = history.firstOrNull()?.createdAt,
                history = history,
                journey = journey.sortedByDescending { it.timestamp }, // Newest first for UI
                relatedWords = related,
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
