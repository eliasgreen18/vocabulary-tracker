package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

sealed class JourneyEvent {
    abstract val timestamp: Instant

    data class Discovery(
        override val timestamp: Instant,
        val bookTitle: String,
        val chapterDisplay: String,
        val snippet: String? = null
    ) : JourneyEvent()

    data class Encounter(
        override val timestamp: Instant,
        val bookTitle: String,
        val chapterDisplay: String,
        val snippet: String? = null
    ) : JourneyEvent()

    data class Reviewed(
        override val timestamp: Instant,
        val nextInterval: Int,
        val successful: Boolean
    ) : JourneyEvent()

    data class Mastered(
        override val timestamp: Instant
    ) : JourneyEvent()
}
