package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.BookDao
import com.eliasgreen18.vocabularytracker.data.local.dao.ReadingSessionDao
import com.eliasgreen18.vocabularytracker.data.local.entity.toDomain
import com.eliasgreen18.vocabularytracker.data.local.entity.toEntity
import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject

class ReadingSessionRepositoryImpl @Inject constructor(
    private val sessionDao: ReadingSessionDao,
    private val bookDao: BookDao
) : ReadingSessionRepository {

    override fun getActiveSessionForBook(bookId: Long): Flow<ReadingSession?> {
        return sessionDao.getActiveSessionForBook(bookId).map { it?.toDomain() }
    }

    override fun getAllActiveSessionsWithDetails(): Flow<List<ActiveSessionInfo>> {
        return combine(
            sessionDao.getAllActiveSessionsWithDetails(),
            bookDao.getAllBooks()
        ) { sessionDetails, books ->
            sessionDetails.map { detail ->
                ActiveSessionInfo(
                    session = detail.session.toDomain(),
                    chapter = detail.chapter.toDomain(),
                    book = books.find { it.id == detail.chapter.bookId }?.toDomain()
                )
            }
        }
    }

    override fun getSessionWithDetailsById(sessionId: Long): Flow<ActiveSessionInfo?> {
        return combine(
            sessionDao.getSessionWithDetailsById(sessionId),
            bookDao.getAllBooks()
        ) { detail, books ->
            detail?.let {
                ActiveSessionInfo(
                    session = it.session.toDomain(),
                    chapter = it.chapter.toDomain(),
                    book = books.find { b -> b.id == it.chapter.bookId }?.toDomain()
                )
            }
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

    override suspend fun endSession(sessionId: Long) {
        val sessionEntity = sessionDao.getSessionById(sessionId)
        sessionEntity?.let {
            val updatedSession = it.copy(endedAt = Instant.now())
            sessionDao.updateSession(updatedSession)
        }
    }
}
