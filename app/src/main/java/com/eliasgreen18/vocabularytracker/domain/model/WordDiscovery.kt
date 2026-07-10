package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class WordDiscovery(
    val wordText: String,
    val firstSeenAt: Instant
)
