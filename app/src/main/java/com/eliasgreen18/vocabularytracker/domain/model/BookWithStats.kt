package com.eliasgreen18.vocabularytracker.domain.model

data class BookWithStats(
    val id: Long,
    val title: String,
    val author: String,
    val language: String,
    val genre: String?,
    val wordCount: Int,
    val chapterCount: Int
)
