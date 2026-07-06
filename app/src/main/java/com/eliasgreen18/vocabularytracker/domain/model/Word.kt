package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class Word(
    val id: Long = 0,
    val text: String,
    val translation: String? = null,
    val isFocusWord: Boolean = false,
    val lastReviewedAt: Long? = null,
    val reviewPriority: Int = 0,
    val translationStatus: TranslationStatus = TranslationStatus.NOT_REQUESTED,
    // SRS Metadata
    val nextReviewAt: Instant? = null,
    val lastSrsReviewAt: Instant? = null,
    val reviewCount: Int = 0,
    val successfulReviews: Int = 0,
    val currentIntervalDays: Int = 0
)
