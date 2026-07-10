package com.eliasgreen18.vocabularytracker.data.local.entity

import java.time.Instant

data class WordHistoryEntity(
    val createdAt: Instant,
    val bookTitle: String,
    val bookLanguage: String,
    val chapterNumber: String, // Changed from Int to String
    val chapterTitle: String?,
    val sessionId: Long,
    val snippet: String?
)
