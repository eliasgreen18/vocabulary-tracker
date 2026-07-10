package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.ChapterDao
import com.eliasgreen18.vocabularytracker.data.local.entity.toDomain
import com.eliasgreen18.vocabularytracker.data.local.entity.toEntity
import com.eliasgreen18.vocabularytracker.domain.model.Chapter
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
    private val chapterDao: ChapterDao
) : ChapterRepository {

    override suspend fun getChapterByNumber(bookId: Long, number: String): Chapter? {
        return chapterDao.getChapterByNumber(bookId, number)?.toDomain()
    }

    override fun getChaptersForBook(bookId: Long): Flow<List<Chapter>> {
        return chapterDao.getChaptersForBook(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChapterById(chapterId: Long): Chapter? {
        return chapterDao.getChapterById(chapterId)?.toDomain()
    }

    override suspend fun insertChapter(chapter: Chapter): Long {
        return chapterDao.insertChapter(chapter.toEntity())
    }

    override suspend fun updateChapter(chapter: Chapter) {
        chapterDao.updateChapter(chapter.toEntity())
    }

    override fun getTotalChaptersCount(): Flow<Int> {
        return chapterDao.getTotalChaptersCount()
    }
}
