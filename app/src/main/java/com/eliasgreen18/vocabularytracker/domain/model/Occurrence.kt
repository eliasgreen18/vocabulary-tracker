package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class Occurrence(
    val id: Long = 0,
    val wordId: Long,
    val sessionId: Long,
    val createdAt: Instant,
    val snippet: String? = null
)
