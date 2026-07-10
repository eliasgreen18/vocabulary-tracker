package com.eliasgreen18.vocabularytracker.data.local.entity

data class BookContributionEntity(
    val bookId: Long,
    val bookTitle: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int
)

data class ChapterDifficultyEntity(
    val chapterId: Long,
    val bookTitle: String,
    val chapterNumber: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int
)
