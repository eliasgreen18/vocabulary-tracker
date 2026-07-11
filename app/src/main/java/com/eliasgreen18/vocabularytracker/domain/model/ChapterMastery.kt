package com.eliasgreen18.vocabularytracker.domain.model

data class ChapterMastery(
    val chapterId: Long,
    val uniqueWordsCount: Int,
    val learnedWordsCount: Int
) {
    val isMastered: Boolean
        get() = uniqueWordsCount > 0 && learnedWordsCount == uniqueWordsCount
}
