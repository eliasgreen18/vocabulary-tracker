package com.eliasgreen18.vocabularytracker.data.local.entity

import java.time.Instant

data class BookWithStatsEntity(
    val id: Long,
    val title: String,
    val author: String,
    val language: String,
    val genre: String?,
    val coverPath: String?,
    val filePath: String?,
    val status: String,
    val wordCount: Int,
    val chapterCount: Int,
    val lastOpenedAt: Instant? = null
)
