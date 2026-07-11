package com.eliasgreen18.vocabularytracker.domain.model

data class BookStats(
    val bookId: Long,
    val bookTitle: String,
    val bookAuthor: String?,
    val bookLanguage: String,
    val bookGenre: String?,
    val bookCoverPath: String?,
    val bookFilePath: String?,
    val bookStatus: BookStatus,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int,
    val learnedWordsCount: Int,
    val topWords: List<WordWithCount>
)
