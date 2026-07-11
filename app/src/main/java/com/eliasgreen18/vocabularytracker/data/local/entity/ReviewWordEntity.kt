package com.eliasgreen18.vocabularytracker.data.local.entity

data class ReviewWordEntity(
    val wordId: Long,
    val wordText: String,
    val globalCount: Int,
    val isFocusWord: Boolean,
    val lastReviewedAt: Long?,
    val reviewPriority: Int,
    val lastBookTitle: String?,
    val lastBookLanguage: String?,
    val lastChapterNumber: String?,
    val lastChapterTitle: String?,
    val snippet: String?,
    val translation: String?,
    val ipa: String?,
    val notes: String?,
    // SRS Columns
    val currentIntervalDays: Int,
    val nextReviewAt: Long?
)
