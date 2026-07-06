package com.eliasgreen18.vocabularytracker.domain.model

data class SessionSummary(
    val totalWords: Int,
    val newWords: Int,
    val learningWords: Int,
    val learnedWords: Int
)
