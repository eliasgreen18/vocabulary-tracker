package com.eliasgreen18.vocabularytracker.domain.repository

import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    suspend fun getChapterByNumber(bookId: Long, number: String): Chapter?
    fun getChaptersForBook(bookId: Long): Flow<List<Chapter>>
    suspend fun getChapterById(chapterId: Long): Chapter?
    suspend fun insertChapter(chapter: Chapter): Long
    suspend fun updateChapter(chapter: Chapter)
}
