package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String,
    val language: String,
    val genre: String? = null,
    val coverPath: String? = null,
    val filePath: String? = null,
    val status: BookStatus = BookStatus.READING,
    val lastOpenedAt: Instant? = null,
    val lastChapterIndex: Int = 0,
    val lastScrollOffset: Int = 0
)
