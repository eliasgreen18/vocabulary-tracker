package com.eliasgreen18.vocabularytracker.domain.model

data class GlobalStats(
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int,
    val newWordsCount: Int,
    val learningWordsCount: Int,
    val learnedWordsCount: Int,
    val translatedWordsCount: Int,
    val totalChaptersCount: Int,
    val completedBooksCount: Int,
    val totalReadingTimeSeconds: Long,
    // SRS Stats
    val totalReviewsDone: Int,
    val successfulReviews: Int,
    val totalReviewAttempts: Int
) {
    val recallAccuracy: Int
        get() = if (totalReviewAttempts > 0) {
            (successfulReviews * 100) / totalReviewAttempts
        } else 0
        
    val wordsPerHour: Int
        get() {
            if (totalReadingTimeSeconds < 60) return 0
            val hours = totalReadingTimeSeconds.toDouble() / 3600.0
            return (uniqueWordsCount.toDouble() / hours).toInt()
        }
}
