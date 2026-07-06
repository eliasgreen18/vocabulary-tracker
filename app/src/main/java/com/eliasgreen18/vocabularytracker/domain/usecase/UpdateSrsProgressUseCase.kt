package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import com.eliasgreen18.vocabularytracker.domain.util.SrsAlgorithm
import java.time.Instant
import javax.inject.Inject

class UpdateSrsProgressUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, remembered: Boolean) {
        val word = repository.getWordById(wordId) ?: return
        
        val now = Instant.now()
        val (nextReviewDate, nextInterval) = SrsAlgorithm.calculateNextReview(
            currentIntervalDays = word.currentIntervalDays,
            remembered = remembered,
            now = now
        )

        repository.updateSrsMetadata(
            wordId = wordId,
            nextReviewAt = nextReviewDate,
            lastReviewAt = now,
            reviewCount = word.reviewCount + 1,
            successfulReviews = if (remembered) word.successfulReviews + 1 else word.successfulReviews,
            currentIntervalDays = nextInterval
        )
    }
}
