package com.eliasgreen18.vocabularytracker.domain.model

data class LearningProgress(
    val totalWords: Int,
    val newCount: Int,
    val learningCount: Int,
    val learnedCount: Int,
    val wordsAddedToday: Int
)
