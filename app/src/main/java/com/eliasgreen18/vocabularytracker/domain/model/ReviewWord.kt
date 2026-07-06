package com.eliasgreen18.vocabularytracker.domain.model

data class ReviewWord(
    val wordId: Long,
    val wordText: String,
    val globalCount: Int,
    val isFocusWord: Boolean,
    val lastReviewedAt: Long?,
    val reviewPriority: Int,
    val lastContext: String
)
