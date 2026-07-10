package com.eliasgreen18.vocabularytracker.data.local.entity

data class BookWithStatsEntity(
    val id: Long,
    val title: String,
    val author: String,
    val language: String,
    val genre: String?,
    val wordCount: Int,
    val chapterCount: Int
)
