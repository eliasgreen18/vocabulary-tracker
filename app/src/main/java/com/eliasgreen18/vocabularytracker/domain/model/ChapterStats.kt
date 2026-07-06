package com.eliasgreen18.vocabularytracker.domain.model

data class ChapterStats(
    val chapterId: Long,
    val uniqueWordsCount: Int,
    val newWordsCount: Int,
    val topWords: List<WordWithCount>
)
