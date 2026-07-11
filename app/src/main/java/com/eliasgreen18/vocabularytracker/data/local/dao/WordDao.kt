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

    @Query("UPDATE words SET text = :newText WHERE id = :wordId")
    suspend fun updateWordText(wordId: Long, newText: String)

    @Query("DELETE FROM words WHERE id = :wordId")
    suspend fun deleteWord(wordId: Long)

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.ipa as ipa,
            w.notes as notes,
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
            (SELECT COUNT(*) FROM occurrences o2 WHERE o2.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.ipa as ipa,
            w.notes as notes,
            w.translationStatus as translationStatus
        FROM words w
        LEFT JOIN occurrences o ON w.id = o.wordId
        LEFT JOIN reading_sessions rs ON o.sessionId = rs.id
        LEFT JOIN chapters c ON rs.chapterId = c.id
        LEFT JOIN books b ON c.bookId = b.id
        WHERE (w.text LIKE '%' || :query || '%' OR w.translation LIKE '%' || :query || '%')
        AND (:bookId IS NULL OR c.bookId = :bookId)
        AND (:author IS NULL OR b.author = :author)
        AND (:isFavorite IS NULL OR w.isFocusWord = :isFavorite)
        GROUP BY w.id
        HAVING (:minHits IS NULL OR globalCount >= :minHits)
        AND (:maxHits IS NULL OR globalCount <= :maxHits)
        ORDER BY globalCount DESC, w.text ASC
    """)
    fun searchWordsWithCount(
        query: String, 
        bookId: Long? = null,
        author: String? = null,
        isFavorite: Boolean? = null,
        minHits: Int? = null,
        maxHits: Int? = null
    ): Flow<List<WordWithCountEntity>>

    @Query("SELECT DISTINCT author FROM books WHERE author IS NOT NULL AND author != ''")
    fun getAllAuthors(): Flow<List<String>>

    @Query("SELECT * FROM words WHERE text LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.ipa as ipa,
            w.notes as notes,
            w.translationStatus as translationStatus
        FROM words w
        ORDER BY globalCount DESC, w.text ASC
    """)
    fun getAllWordsWithCount(): Flow<List<WordWithCountEntity>>

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    fun getTotalWordsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE translationStatus = 'DONE'")
    fun getTranslatedWordsCount(): Flow<Int>

    @Query("UPDATE words SET translation = :translation, translationStatus = :status WHERE id = :wordId")
    suspend fun updateTranslation(wordId: Long, translation: String?, status: String)

    @Query("UPDATE words SET ipa = :ipa WHERE id = :wordId")
    suspend fun updateIpa(wordId: Long, ipa: String?)

    @Query("UPDATE words SET notes = :notes WHERE id = :wordId")
    suspend fun updateNotes(wordId: Long, notes: String?)

    @Query("UPDATE words SET aiExplanation = :explanation, aiExamples = :examples WHERE id = :wordId")
    suspend fun updateAiInsights(wordId: Long, explanation: String?, examples: String?)

    @Query("SELECT * FROM words WHERE translationStatus IN ('PENDING', 'LOADING', 'ERROR')")
    fun getPendingTranslations(): Flow<List<WordEntity>>

    @Query("UPDATE words SET lastReviewedAt = :timestamp, reviewPriority = 0 WHERE id = :wordId")
    suspend fun markReviewed(wordId: Long, timestamp: Long)

    @Query("UPDATE words SET reviewPriority = reviewPriority + 1 WHERE id = :wordId")
    suspend fun markNotRemembered(wordId: Long)

    @Query("""
        UPDATE words 
        SET nextReviewAt = :nextReviewAt, 
            lastSrsReviewAt = :lastReviewAt, 
            reviewCount = :reviewCount, 
            successfulReviews = :successfulReviews, 
            currentIntervalDays = :currentIntervalDays 
        WHERE id = :wordId
    """)
    suspend fun updateSrsMetadata(
        wordId: Long,
        nextReviewAt: Long?,
        lastReviewAt: Long?,
        reviewCount: Int,
        successfulReviews: Int,
        currentIntervalDays: Int
    )

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.lastReviewedAt as lastReviewedAt,
            w.reviewPriority as reviewPriority,
            b.title as lastBookTitle,
            b.language as lastBookLanguage,
            c.number as lastChapterNumber,
            c.title as lastChapterTitle,
            last_o.snippet as snippet,
            w.translation as translation,
            w.ipa as ipa,
            w.notes as notes,
            w.currentIntervalDays as currentIntervalDays,
            w.nextReviewAt as nextReviewAt
        FROM words w
        LEFT JOIN (
            SELECT wordId, sessionId, snippet, MAX(createdAt)
            FROM occurrences 
            GROUP BY wordId
        ) last_o ON w.id = last_o.wordId
        LEFT JOIN reading_sessions rs ON last_o.sessionId = rs.id
        LEFT JOIN chapters c ON rs.chapterId = c.id
        LEFT JOIN books b ON c.bookId = b.id
        WHERE (globalCount >= 3 OR w.isFocusWord = 1)
        AND (w.nextReviewAt IS NULL OR w.nextReviewAt <= :now)
        ORDER BY w.reviewPriority DESC, globalCount DESC, w.id ASC
    """)
    fun getDueWords(now: Long): Flow<List<com.eliasgreen18.vocabularytracker.data.local.entity.ReviewWordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE reviewCount > 0")
    fun getTotalReviewsDoneCount(): Flow<Int>

    @Query("SELECT SUM(successfulReviews) FROM words")
    fun getTotalSuccessfulReviewsCount(): Flow<Int>

    @Query("SELECT SUM(reviewCount) FROM words")
    fun getTotalReviewAttemptsCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT wordId FROM occurrences 
            GROUP BY wordId 
            HAVING COUNT(*) >= :minHits AND COUNT(*) <= :maxHits
        )
    """)
    fun getWordsByHitsRangeCount(minHits: Int, maxHits: Int): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT wordId FROM occurrences 
            GROUP BY wordId 
            HAVING COUNT(*) >= :minHits
        )
    """)
    fun getWordsAboveHitsCount(minHits: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE reviewPriority > 2")
    fun getForgottenWordsCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT rs.chapterId
            FROM occurrences o
            JOIN reading_sessions rs ON o.sessionId = rs.id
            GROUP BY rs.chapterId
            HAVING COUNT(DISTINCT o.wordId) > 0 
            AND COUNT(DISTINCT CASE WHEN (SELECT COUNT(*) FROM occurrences o2 WHERE o2.wordId = o.wordId) >= 3 THEN o.wordId END) = COUNT(DISTINCT o.wordId)
        )
    """)
    fun getTotalMasteredChaptersCount(): Flow<Int>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): WordEntity?

    @Query("""
        SELECT 
            b.author as author,
            COUNT(DISTINCT o.wordId) as uniqueWordsCount,
            COUNT(o.id) as totalOccurrencesCount
        FROM occurrences o
        JOIN reading_sessions rs ON o.sessionId = rs.id
        JOIN chapters c ON rs.chapterId = c.id
        JOIN books b ON c.bookId = b.id
        GROUP BY b.author
        ORDER BY uniqueWordsCount DESC
    """)
    fun getAuthorVocabularyStats(): Flow<List<com.eliasgreen18.vocabularytracker.data.local.entity.AuthorStatsEntity>>
}
