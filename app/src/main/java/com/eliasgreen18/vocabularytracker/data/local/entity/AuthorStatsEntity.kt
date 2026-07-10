package com.eliasgreen18.vocabularytracker.data.local.entity

data class AuthorStatsEntity(
    val author: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int
)
