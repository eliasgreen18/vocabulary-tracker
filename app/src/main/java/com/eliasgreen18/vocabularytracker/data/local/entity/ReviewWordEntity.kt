package com.eliasgreen18.vocabularytracker.data.local.entity

data class ReviewWordEntity(
    val wordId: Long,
    val wordText: String,
    val globalCount: Int,
    val isFocusWord: Boolean,
    val lastReviewedAt: Long?,
    val reviewPriority: Int,
    val lastBookTitle: String?,
    val lastChapterNumber: Int?,
    val lastChapterTitle: String?
)
