package com.eliasgreen18.vocabularytracker.domain.model

data class GlobalStats(
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int,
    val newWordsCount: Int,
    val learningWordsCount: Int,
    val learnedWordsCount: Int,
    val translatedWordsCount: Int
)
