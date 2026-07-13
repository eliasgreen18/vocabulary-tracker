package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.BookDao
import com.eliasgreen18.vocabularytracker.data.local.dao.ReadingSessionDao
import com.eliasgreen18.vocabularytracker.data.mapper.toDomain
import com.eliasgreen18.vocabularytracker.data.mapper.toEntity
import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class ReadingSessionRepositoryImpl @Inject constructor(
    private val sessionDao: ReadingSessionDao,
    private val bookDao: BookDao
) : ReadingSessionRepository {

    override fun getActiveSessionForBook(bookId: Long): Flow<ReadingSession?> {
        return sessionDao.getActiveSessionForBook(bookId).map { it?.toDomain() }
    }

    override fun getAllActiveSessionsWithDetails(): Flow<List<ActiveSessionInfo>> {
        return sessionDao.getAllActiveSessionsWithDetails().map { entities ->
            val books = bookDao.getAllBooks().first()
            entities.map { detail ->
                detail.toDomain(books.find { it.id == detail.chapter.bookId }?.toDomain())
            }
        }
    }

    override fun getSessionWithDetailsById(sessionId: Long): Flow<ActiveSessionInfo?> {
        return sessionDao.getSessionWithDetailsById(sessionId).map { detail ->
            val books = bookDao.getAllBooks().first()
            detail?.toDomain(books.find { it.id == detail.chapter.bookId }?.toDomain())
        }
    }

    override suspend fun getSessionById(sessionId: Long): ReadingSession? {
        return sessionDao.getSessionById(sessionId)?.toDomain()
    }

    override suspend fun insertSession(session: ReadingSession): Long {
        return sessionDao.insertSession(session.toEntity())
    }

    override suspend fun updateSession(session: ReadingSession) {
        sessionDao.updateSession(session.toEntity())
    }

    override suspend fun updateSessionDuration(sessionId: Long, duration: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(session.copy(activeDurationSeconds = duration))
    }

    override suspend fun endSession(sessionId: Long, activeDurationSeconds: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updatedSession = session.copy(
            endedAt = Instant.now(),
            activeDurationSeconds = activeDurationSeconds
        )
        sessionDao.updateSession(updatedSession)
    }

    override fun getTotalReadingTimeSeconds(): Flow<Long> {
        return sessionDao.getTotalReadingTimeSeconds().map { it ?: 0L }
    }

    override fun getDailyReadingDurations(since: Instant): Flow<Map<LocalDate, Long>> {
        return sessionDao.getDailyReadingDurations(since.toEpochMilli()).map { entities ->
            entities.associate { LocalDate.parse(it.date) to it.totalSeconds }
        }
    }
}
