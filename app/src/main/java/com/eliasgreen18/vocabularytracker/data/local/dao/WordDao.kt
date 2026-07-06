package com.eliasgreen18.vocabularytracker.data.local.dao

import androidx.room.*
import com.eliasgreen18.vocabularytracker.data.local.entity.WordEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.WordWithCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE text = :text")
    suspend fun getWordByText(text: String): WordEntity?

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE id = :id")
    fun getWordByIdFlow(id: Long): Flow<WordEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: WordEntity): Long

    @Query("UPDATE words SET isFocusWord = :isFocus WHERE id = :wordId")
    suspend fun updateFocusStatus(wordId: Long, isFocus: Boolean)

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM words w
        WHERE w.isFocusWord = 1
        ORDER BY globalCount DESC, w.text ASC
    """)
    fun getFocusWordsWithCount(): Flow<List<WordWithCountEntity>>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus
        FROM words w
        WHERE w.text LIKE '%' || :query || '%'
        ORDER BY globalCount DESC, w.text ASC
    """)
    fun searchWordsWithCount(query: String): Flow<List<WordWithCountEntity>>

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    fun getTotalWordsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE translationStatus = 'DONE'")
    fun getTranslatedWordsCount(): Flow<Int>

    @Query("UPDATE words SET translation = :translation, translationStatus = :status WHERE id = :wordId")
    suspend fun updateTranslation(wordId: Long, translation: String?, status: String)

    @Query("SELECT * FROM words WHERE translationStatus IN ('PENDING', 'LOADING', 'ERROR')")
    fun getPendingTranslations(): Flow<List<WordEntity>>

    @Query("UPDATE words SET lastReviewedAt = :timestamp, reviewPriority = 0 WHERE id = :wordId")
    suspend fun markReviewed(wordId: Long, timestamp: Long)

    @Query("UPDATE words SET reviewPriority = reviewPriority + 1 WHERE id = :wordId")
    suspend fun markNotRemembered(wordId: Long)

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.lastReviewedAt as lastReviewedAt,
            w.reviewPriority as reviewPriority,
            b.title as lastBookTitle,
            c.number as lastChapterNumber,
            c.title as lastChapterTitle
        FROM words w
        LEFT JOIN (
            SELECT wordId, sessionId, MAX(createdAt) 
            FROM occurrences 
            GROUP BY wordId
        ) last_o ON w.id = last_o.wordId
        LEFT JOIN reading_sessions rs ON last_o.sessionId = rs.id
        LEFT JOIN chapters c ON rs.chapterId = c.id
        LEFT JOIN books b ON c.bookId = b.id
        WHERE (globalCount >= 3 OR w.isFocusWord = 1)
        AND (w.lastReviewedAt IS NULL OR w.lastReviewedAt < :startOfToday)
        ORDER BY w.reviewPriority DESC, globalCount DESC, w.id ASC
    """)
    fun getReviewQueue(startOfToday: Long): Flow<List<com.eliasgreen18.vocabularytracker.data.local.entity.ReviewWordEntity>>
}
