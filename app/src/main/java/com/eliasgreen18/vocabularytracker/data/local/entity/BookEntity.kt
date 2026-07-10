package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.Book
import java.time.Instant

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val language: String,
    val genre: String? = null, // New column
    val lastOpenedAt: Instant? = null
)

fun BookEntity.toDomain() = Book(
    id = id,
    title = title,
    author = author,
    language = language,
    genre = genre,
    lastOpenedAt = lastOpenedAt
)

fun Book.toEntity() = BookEntity(
    id = id,
    title = title,
    author = author,
    language = language,
    genre = genre,
    lastOpenedAt = lastOpenedAt
)
