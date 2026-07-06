package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class WordOccurrenceDetail(
    val createdAt: Instant,
    val bookTitle: String,
    val chapterNumber: Int,
    val chapterTitle: String?
) {
    val displayChapter: String
        get() = if (chapterTitle.isNullOrBlank()) {
            "Chapter $chapterNumber"
        } else {
            "Chapter $chapterNumber: $chapterTitle"
        }
}
