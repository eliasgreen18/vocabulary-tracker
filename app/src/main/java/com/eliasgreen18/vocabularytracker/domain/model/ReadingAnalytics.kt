package com.eliasgreen18.vocabularytracker.domain.model

data class ReadingAnalytics(
    val wordsPerBook: List<BookContribution>,
    val wordsPerChapter: List<ChapterDifficulty>,
    val mostChallengingChapter: ChapterDifficulty?,
    val topContributingBook: BookContribution?
)

data class BookContribution(
    val bookId: Long,
    val bookTitle: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int
)

data class ChapterDifficulty(
    val chapterId: Long,
    val bookTitle: String,
    val chapterNumber: String,
    val uniqueWordsCount: Int,
    val totalOccurrencesCount: Int
)
