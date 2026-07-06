package com.eliasgreen18.vocabularytracker.data.local.dao

import androidx.room.*
import com.eliasgreen18.vocabularytracker.data.local.entity.OccurrenceEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.WordHistoryEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.WordWithCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OccurrenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: OccurrenceEntity): Long

    @Query("SELECT COUNT(*) FROM occurrences WHERE wordId = :wordId")
    fun getOccurrenceCountForWord(wordId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM occurrences")
    fun getTotalOccurrencesCount(): Flow<Int>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText, 
            COUNT(CASE WHEN o.sessionId = :sessionId THEN 1 END) as sessionCount,
            (SELECT COUNT(*) FROM occurrences o2 WHERE o2.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM occurrences o
        JOIN words w ON o.wordId = w.id
        WHERE o.sessionId = :sessionId
        GROUP BY w.id
        ORDER BY MAX(o.createdAt) DESC
    """)
    fun getSessionWordsWithCounts(sessionId: Long): Flow<List<WordWithCountEntity>>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            COUNT(o.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM occurrences o
        JOIN words w ON o.wordId = w.id
        JOIN reading_sessions rs ON o.sessionId = rs.id
        JOIN chapters c ON rs.chapterId = c.id
        WHERE c.bookId = :bookId
        GROUP BY w.id
        ORDER BY globalCount DESC
        LIMIT :limit
    """)
    fun getTopWordsForBook(bookId: Long, limit: Int): Flow<List<WordWithCountEntity>>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            COUNT(o.id) as sessionCount,
            (SELECT COUNT(*) FROM occurrences o2 WHERE o2.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM occurrences o
        JOIN words w ON o.wordId = w.id
        WHERE o.sessionId IN (SELECT id FROM reading_sessions WHERE chapterId = :chapterId)
        GROUP BY w.id
        ORDER BY sessionCount DESC
        LIMIT :limit
    """)
    fun getTopWordsForChapter(chapterId: Long, limit: Int): Flow<List<WordWithCountEntity>>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            COUNT(o.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM words w
        JOIN occurrences o ON w.id = o.wordId
        JOIN reading_sessions rs ON o.sessionId = rs.id
        JOIN chapters c ON rs.chapterId = c.id
        WHERE c.bookId = :bookId
        GROUP BY w.id
    """)
    fun getWordsForBook(bookId: Long): Flow<List<WordWithCountEntity>>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText, 
            COUNT(CASE WHEN rs.chapterId = :chapterId THEN 1 END) as sessionCount,
            (SELECT COUNT(*) FROM occurrences o2 WHERE o2.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM occurrences o
        JOIN words w ON o.wordId = w.id
        JOIN reading_sessions rs ON o.sessionId = rs.id
        WHERE rs.chapterId = :chapterId
        GROUP BY w.id
        ORDER BY MAX(o.createdAt) DESC
    """)
    fun getChapterWordsWithCounts(chapterId: Long): Flow<List<WordWithCountEntity>>

    @Query("""
        SELECT 
            o.createdAt,
            b.title as bookTitle,
            b.language as bookLanguage,
            c.number as chapterNumber,
            c.title as chapterTitle,
            o.sessionId as sessionId
        FROM occurrences o
        JOIN reading_sessions rs ON o.sessionId = rs.id
        JOIN chapters c ON rs.chapterId = c.id
        JOIN books b ON c.bookId = b.id
        WHERE o.wordId = :wordId
        ORDER BY o.createdAt DESC
    """)
    fun getWordHistory(wordId: Long): Flow<List<WordHistoryEntity>>

    @Query("SELECT COUNT(DISTINCT wordId) FROM occurrences WHERE createdAt >= :timestamp")
    fun getUniqueWordsCountSince(timestamp: Long): Flow<Int>
}
