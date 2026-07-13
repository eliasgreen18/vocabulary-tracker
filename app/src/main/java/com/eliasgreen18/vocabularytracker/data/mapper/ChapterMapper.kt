package com.eliasgreen18.vocabularytracker.data.mapper

import com.eliasgreen18.vocabularytracker.data.local.entity.ChapterEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.ChapterMasteryEntity
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.model.ChapterMastery

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

fun ChapterMasteryEntity.toDomain() = ChapterMastery(
    chapterId = chapterId,
    uniqueWordsCount = uniqueWordsCount,
    learnedWordsCount = learnedWordsCount
)
