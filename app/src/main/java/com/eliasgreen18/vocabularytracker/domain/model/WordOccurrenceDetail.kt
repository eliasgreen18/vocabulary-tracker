package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class WordOccurrenceDetail(
    val createdAt: Instant,
    val bookTitle: String,
    val bookLanguage: String,
    val chapterNumber: Int,
    val chapterTitle: String?,
    val sessionId: Long
) {
    val displayChapter: String
        get() = if (chapterTitle.isNullOrBlank()) {
            "Chapter $chapterNumber"
        } else {
            "Chapter $chapterNumber: $chapterTitle"
        }
    
    val displaySession: String
        get() = "Session #$sessionId"
}
