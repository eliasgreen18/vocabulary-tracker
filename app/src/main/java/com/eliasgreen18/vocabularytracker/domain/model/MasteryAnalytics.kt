package com.eliasgreen18.vocabularytracker.domain.model

data class MasteryAnalytics(
    val distribution: MasteryDistribution,
    val recallAccuracy: Int,
    val forgottenWordsCount: Int,
    val learningVelocity: Int // Words mastered in last 30 days
)

data class MasteryDistribution(
    val newCount: Int,
    val learningCount: Int,
    val learnedCount: Int,
    val totalCount: Int
)
