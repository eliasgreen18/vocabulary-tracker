package com.eliasgreen18.vocabularytracker.domain.model

data class Word(
    val id: Long = 0,
    val text: String,
    val translation: String? = null,
    val isFocusWord: Boolean = false,
    val lastReviewedAt: Long? = null,
    val reviewPriority: Int = 0,
    val translationStatus: TranslationStatus = TranslationStatus.NOT_REQUESTED
)
