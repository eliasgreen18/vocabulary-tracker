package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String,
    val language: String,
    val genre: String? = null, // New field
    val lastOpenedAt: Instant? = null
)
