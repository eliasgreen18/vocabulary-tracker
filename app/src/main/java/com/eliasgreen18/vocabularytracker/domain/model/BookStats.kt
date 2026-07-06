package com.eliasgreen18.vocabularytracker.domain.model

data class BookStats(
    val bookId: Long,
    val bookTitle: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int,
    val learnedWordsCount: Int,
    val topWords: List<WordWithCount>
)
