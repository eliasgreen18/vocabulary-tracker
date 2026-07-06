package com.eliasgreen18.vocabularytracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eliasgreen18.vocabularytracker.domain.model.Chapter

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["bookId", "number"], unique = true)]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val number: Int,
    val title: String?
)

fun ChapterEntity.toDomain() = Chapter(
    id = id,
    bookId = bookId,
    number = number,
    title = title
)

fun Chapter.toEntity() = ChapterEntity(
    id = id,
    bookId = bookId,
    number = number,
    title = title
)
