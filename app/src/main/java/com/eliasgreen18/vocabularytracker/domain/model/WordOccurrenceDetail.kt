package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class WordOccurrenceDetail(
    val createdAt: Instant,
    val bookTitle: String,
    val bookLanguage: String,
    val chapterNumber: String, // Changed from Int to String
    val chapterTitle: String?,
    val sessionId: Long,
    val snippet: String? = null
) {
    val displayChapter: String
        get() {
            val prefix = if (chapterNumber.all { it.isDigit() }) "Chapter $chapterNumber" else chapterNumber
            return if (chapterTitle.isNullOrBlank()) {
                prefix
            } else {
                "$prefix: $chapterTitle"
            }
        }
    
    val displaySession: String
        get() = "Session #$sessionId"
}
