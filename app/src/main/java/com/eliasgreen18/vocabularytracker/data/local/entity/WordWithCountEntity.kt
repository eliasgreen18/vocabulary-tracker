package com.eliasgreen18.vocabularytracker.data.local.entity

data class WordWithCountEntity(
    val wordId: Long,
    val wordText: String,
    val sessionCount: Int,
    val globalCount: Int,
    val isFocusWord: Boolean,
    val translation: String? = null,
    val ipa: String? = null,
    val notes: String? = null,
    val translationStatus: String = "NOT_REQUESTED"
)
