package com.eliasgreen18.vocabularytracker.data.mapper

import com.eliasgreen18.vocabularytracker.data.local.entity.BookContributionEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.ChapterDifficultyEntity
import com.eliasgreen18.vocabularytracker.domain.model.BookContribution
import com.eliasgreen18.vocabularytracker.domain.model.ChapterDifficulty

fun BookContributionEntity.toDomain() = BookContribution(
    bookId = bookId,
    bookTitle = bookTitle,
    uniqueWordsCount = uniqueWordsCount,
    totalOccurrencesCount = totalOccurrencesCount
)

fun ChapterDifficultyEntity.toDomain() = ChapterDifficulty(
    chapterId = chapterId,
    bookTitle = bookTitle,
    chapterNumber = chapterNumber,
    uniqueWordsCount = uniqueWordsCount,
    totalOccurrencesCount = totalOccurrencesCount
)
