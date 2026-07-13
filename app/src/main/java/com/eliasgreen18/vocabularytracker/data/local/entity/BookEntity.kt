package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.BookStatus
import java.time.Instant

@Entity(
    tableName = "books",
    indices = [Index("author")]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val language: String,
    val genre: String? = null,
    val coverPath: String? = null,
    val filePath: String? = null,
    val status: String = BookStatus.READING.name,
    val lastOpenedAt: Instant? = null,
    val lastChapterIndex: Int = 0,
    val lastScrollOffset: Int = 0
)
