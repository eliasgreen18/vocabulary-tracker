package com.eliasgreen18.vocabularytracker.domain.model

data class ActiveSessionInfo(
    val session: ReadingSession,
    val book: Book?, // Book might be null in raw session queries, though usually available
    val chapter: Chapter
)
