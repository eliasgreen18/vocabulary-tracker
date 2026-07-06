package com.eliasgreen18.vocabularytracker.data.local.entity

import java.time.Instant

data class WordHistoryEntity(
    val createdAt: Instant,
    val bookTitle: String,
    val bookLanguage: String,
    val chapterNumber: Int,
    val chapterTitle: String?,
    val sessionId: Long
)
