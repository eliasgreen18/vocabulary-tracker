package com.eliasgreen18.vocabularytracker.domain.repository

import com.eliasgreen18.vocabularytracker.domain.model.Occurrence
import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.model.WordOccurrenceDetail
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    suspend fun getWordByText(text: String): Word?
    suspend fun getWordById(id: Long): Word?
    fun getWordByIdFlow(id: Long): Flow<Word?>
    suspend fun insertWord(word: Word): Long
    suspend fun updateWordText(wordId: Long, newText: String)
    suspend fun incrementOccurrenceCount(wordId: Long)
    suspend fun updateIpa(wordId: Long, ipa: String?)
    suspend fun updateNotes(wordId: Long, notes: String?)
    suspend fun updateAiInsights(wordId: Long, explanation: String?, examples: String?)
    suspend fun deleteWord(wordId: Long)
    suspend fun insertOccurrence(occurrence: Occurrence): Long
    suspend fun deleteLatestOccurrenceInSession(wordId: Long, sessionId: Long)
    suspend fun getOccurrenceCountSync(wordId: Long): Int
    fun getOccurrenceCountForWord(wordId: Long): Flow<Int>
    fun getSessionWords(sessionId: Long): Flow<List<WordWithCount>>
    fun getChapterWords(chapterId: Long): Flow<List<WordWithCount>>
    fun getWordHistory(wordId: Long): Flow<List<WordOccurrenceDetail>>
    fun searchWords(
        query: String, 
        bookId: Long? = null,
        author: String? = null,
        isFavorite: Boolean? = null,
        minHits: Int? = null,
        maxHits: Int? = null
    ): Flow<List<WordWithCount>>
    fun getAllWordsWithCount(): Flow<List<WordWithCount>>
    fun getFocusWords(): Flow<List<WordWithCount>>
    suspend fun updateFocusStatus(wordId: Long, isFocus: Boolean)
    fun getTotalWordsCount(): Flow<Int>
    fun getUniqueWordsCountSince(timestamp: Long): Flow<Int>
    fun getDueWords(now: Long): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.ReviewWord>>
    suspend fun markWordReviewed(wordId: Long, timestamp: Long)
    suspend fun markWordNotRemembered(wordId: Long)
    suspend fun updateSrsMetadata(
        wordId: Long,
        nextReviewAt: java.time.Instant,
        lastReviewAt: java.time.Instant,
        reviewCount: Int,
        successfulReviews: Int,
        currentIntervalDays: Int
    )
    suspend fun updateTranslation(wordId: Long, translation: String?, status: com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus)
    fun getPendingTranslations(): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.Word>>
    fun getTotalReviewsDoneCount(): Flow<Int>
    fun getTotalSuccessfulReviewsCount(): Flow<Int>
    fun getTotalReviewAttemptsCount(): Flow<Int>
    fun getTotalOccurrencesCount(): Flow<Int>
    fun getTranslatedWordsCount(): Flow<Int>
    fun getTopWordsForBook(bookId: Long, limit: Int): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.WordWithCount>>
    fun getTopWordsForChapter(chapterId: Long, limit: Int): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.WordWithCount>>
    fun getWordsForBook(bookId: Long): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.WordWithCount>>
    
    // Analytics
    fun getBookContributions(): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.BookContribution>>
    fun getChapterDifficulties(): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.ChapterDifficulty>>
    fun getDailyActivity(): Flow<Map<java.time.LocalDate, Int>>
    fun getWordsByHitsRangeCount(min: Int, max: Int): Flow<Int>
    fun getWordsAboveHitsCount(min: Int): Flow<Int>
    fun getForgottenWordsCount(): Flow<Int>
    fun getAuthorStats(): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.AuthorStats>>
    fun getAllAuthors(): Flow<List<String>>
    fun getWordDiscoveries(): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.WordDiscovery>>
    fun getChapterMastery(bookId: Long): Flow<Map<Long, com.eliasgreen18.vocabularytracker.domain.model.ChapterMastery>>
    fun getTotalMasteredChaptersCount(): Flow<Int>
    suspend fun getRandomWord(): com.eliasgreen18.vocabularytracker.domain.model.Word?
    
    // Relationships
    suspend fun addRelationship(wordId: Long, relatedId: Long, type: com.eliasgreen18.vocabularytracker.domain.model.RelationshipType)
    suspend fun deleteRelationship(wordId: Long, relatedId: Long, type: com.eliasgreen18.vocabularytracker.domain.model.RelationshipType)
    fun getRelatedWords(wordId: Long): Flow<List<com.eliasgreen18.vocabularytracker.domain.model.RelatedWord>>
}
