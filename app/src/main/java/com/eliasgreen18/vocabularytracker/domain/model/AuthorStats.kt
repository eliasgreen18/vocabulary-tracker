package com.eliasgreen18.vocabularytracker.domain.model

data class AuthorStats(
    val author: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int
)
