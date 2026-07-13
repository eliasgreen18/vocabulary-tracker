package com.eliasgreen18.vocabularytracker.data.mapper

import com.eliasgreen18.vocabularytracker.data.local.entity.BookEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.BookWithStatsEntity
import com.eliasgreen18.vocabularytracker.domain.model.Book
import com.eliasgreen18.vocabularytracker.domain.model.BookStatus
import com.eliasgreen18.vocabularytracker.domain.model.BookWithStats

fun BookEntity.toDomain() = Book(
    id = id,
    title = title,
    author = author,
    language = language,
    genre = genre,
    coverPath = coverPath,
    filePath = filePath,
    status = status.toBookStatus(),
    lastOpenedAt = lastOpenedAt,
    lastChapterIndex = lastChapterIndex,
    lastScrollOffset = lastScrollOffset
)

fun Book.toEntity() = BookEntity(
    id = id,
    title = title,
    author = author,
    language = language,
    genre = genre,
    coverPath = coverPath,
    filePath = filePath,
    status = status.name,
    lastOpenedAt = lastOpenedAt,
    lastChapterIndex = lastChapterIndex,
    lastScrollOffset = lastScrollOffset
)

fun BookWithStatsEntity.toDomain() = BookWithStats(
    id = id,
    title = title,
    author = author,
    language = language,
    genre = genre,
    coverPath = coverPath,
    filePath = filePath,
    status = status.toBookStatus(),
    wordCount = wordCount,
    chapterCount = chapterCount,
    lastOpenedAt = lastOpenedAt
)

private fun String.toBookStatus(): BookStatus {
    return try {
        BookStatus.valueOf(this)
    } catch (e: Exception) {
        BookStatus.READING
    }
}
