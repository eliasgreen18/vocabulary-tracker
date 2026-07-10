package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.MasteryAnalytics
import com.eliasgreen18.vocabularytracker.domain.model.MasteryDistribution
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetMasteryAnalyticsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<MasteryAnalytics> {
        return combine(
            repository.getWordsByHitsRangeCount(0, 2), // New
            repository.getWordsByHitsRangeCount(3, 9), // Learning
            repository.getWordsAboveHitsCount(10),     // Learned
            repository.getTotalSuccessfulReviewsCount(),
            repository.getTotalReviewAttemptsCount(),
            repository.getForgottenWordsCount()
        ) { flows ->
            val new = flows[0] as Int
            val learning = flows[1] as Int
            val learned = flows[2] as Int
            val success = flows[3] as Int
            val attempts = flows[4] as Int
            val forgotten = flows[5] as Int

            val total = new + learning + learned
            val accuracy = if (attempts > 0) (success * 100) / attempts else 0
            
            MasteryAnalytics(
                distribution = MasteryDistribution(
                    newCount = new,
                    learningCount = learning,
                    learnedCount = learned,
                    totalCount = total
                ),
                recallAccuracy = accuracy,
                forgottenWordsCount = forgotten,
                learningVelocity = learned // Simple metric for now
            )
        }
    }
}
