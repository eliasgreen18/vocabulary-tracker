package com.eliasgreen18.vocabularytracker.data.local.dao

import androidx.room.*
import com.eliasgreen18.vocabularytracker.data.local.entity.ChapterEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

data class ActiveSessionWithDetails(
    @Embedded val session: ReadingSessionEntity,
    @Relation(
        parentColumn = "chapterId",
        entityColumn = "id"
    )
    val chapter: ChapterEntity
)

@Dao
interface ReadingSessionDao {
    @Query("SELECT * FROM reading_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): ReadingSessionEntity?

    @Query("SELECT * FROM reading_sessions WHERE endedAt IS NULL")
    fun getAllActiveSessionsWithDetails(): Flow<List<ActiveSessionWithDetails>>

    @Query("SELECT * FROM reading_sessions WHERE id = :sessionId")
    fun getSessionWithDetailsById(sessionId: Long): Flow<ActiveSessionWithDetails?>

    @Query("""
        SELECT rs.* FROM reading_sessions rs
        JOIN chapters c ON rs.chapterId = c.id
        WHERE c.bookId = :bookId AND rs.endedAt IS NULL
        LIMIT 1
    """)
    fun getActiveSessionForBook(bookId: Long): Flow<ReadingSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSessionEntity): Long

    @Update
    suspend fun updateSession(session: ReadingSessionEntity)
}
