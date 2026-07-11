package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class ReadingSession(
    val id: Long = 0,
    val chapterId: Long,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val activeDurationSeconds: Long = 0
)
