package com.eliasgreen18.vocabularytracker.domain.model

data class GlobalStats(
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int,
    val newWordsCount: Int,
    val learningWordsCount: Int,
    val learnedWordsCount: Int,
    val translatedWordsCount: Int,
    // SRS Stats
    val totalReviewsDone: Int,
    val successfulReviews: Int,
    val totalReviewAttempts: Int
) {
    val recallAccuracy: Int
        get() = if (totalReviewAttempts > 0) {
            (successfulReviews * 100) / totalReviewAttempts
        } else 0
}
