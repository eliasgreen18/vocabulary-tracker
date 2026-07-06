package com.eliasgreen18.vocabularytracker.domain.repository

import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import kotlinx.coroutines.flow.Flow

interface ReadingSessionRepository {
    fun getActiveSessionForBook(bookId: Long): Flow<ReadingSession?>
    fun getAllActiveSessionsWithDetails(): Flow<List<ActiveSessionInfo>>
    fun getSessionWithDetailsById(sessionId: Long): Flow<ActiveSessionInfo?>
    suspend fun getSessionById(sessionId: Long): ReadingSession?
    suspend fun insertSession(session: ReadingSession): Long
    suspend fun updateSession(session: ReadingSession)
    suspend fun endSession(sessionId: Long)
}
