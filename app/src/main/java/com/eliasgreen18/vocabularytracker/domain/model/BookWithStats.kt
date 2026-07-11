package com.eliasgreen18.vocabularytracker.domain.model

import java.time.Instant

data class BookWithStats(
    val id: Long,
    val title: String,
    val author: String,
    val language: String,
    val genre: String?,
    val coverPath: String?,
    val filePath: String?,
    val status: BookStatus,
    val wordCount: Int,
    val chapterCount: Int,
    val lastOpenedAt: Instant? = null
)
