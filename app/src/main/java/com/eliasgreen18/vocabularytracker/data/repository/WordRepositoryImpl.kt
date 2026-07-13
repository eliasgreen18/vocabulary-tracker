package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.OccurrenceDao
import com.eliasgreen18.vocabularytracker.data.local.dao.WordDao
import com.eliasgreen18.vocabularytracker.data.mapper.toDomain
import com.eliasgreen18.vocabularytracker.data.mapper.toEntity
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val occurrenceDao: OccurrenceDao
) : WordRepository {

    override suspend fun getWordByText(text: String): Word? {
        return wordDao.getWordByText(text)?.toDomain()
    }

    override suspend fun getWordById(id: Long): Word? {
        return wordDao.getWordById(id)?.toDomain()
    }

    override fun getWordByIdFlow(id: Long): Flow<Word?> {
        return wordDao.getWordByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertWord(word: Word): Long {
        return wordDao.insertWord(word.toEntity())
    }

    override suspend fun updateWordText(wordId: Long, newText: String) {
        wordDao.updateWordText(wordId, newText)
    }

    override suspend fun incrementOccurrenceCount(wordId: Long) {
        wordDao.incrementOccurrenceCount(wordId)
    }

    override suspend fun updateIpa(wordId: Long, ipa: String?) {
        wordDao.updateIpa(wordId, ipa)
    }

    override suspend fun updateNotes(wordId: Long, notes: String?) {
        wordDao.updateNotes(wordId, notes)
    }

    override suspend fun updateAiInsights(wordId: Long, explanation: String?, examples: String?) {
        wordDao.updateAiInsights(wordId, explanation, examples)
    }

    override suspend fun deleteWord(wordId: Long) {
        wordDao.deleteWord(wordId)
    }

    override suspend fun insertOccurrence(occurrence: Occurrence): Long {
        return occurrenceDao.insertOccurrence(occurrence.toEntity())
    }

    override suspend fun deleteLatestOccurrenceInSession(wordId: Long, sessionId: Long) {
        occurrenceDao.deleteLatestOccurrenceInSession(wordId, sessionId)
    }

    override suspend fun getOccurrenceCountSync(wordId: Long): Int {
        return wordDao.getWordById(wordId)?.globalCount ?: 0
    }

    override fun getOccurrenceCountForWord(wordId: Long): Flow<Int> {
        return wordDao.getWordByIdFlow(wordId).map { it?.globalCount ?: 0 }
    }

    override fun getSessionWords(sessionId: Long): Flow<List<WordWithCount>> {
        return occurrenceDao.getSessionWordsWithCounts(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChapterWords(chapterId: Long): Flow<List<WordWithCount>> {
        return occurrenceDao.getChapterWordsWithCounts(chapterId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getWordHistory(wordId: Long): Flow<List<WordOccurrenceDetail>> {
        return occurrenceDao.getWordHistory(wordId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchWords(
        query: String, 
        bookId: Long?,
        author: String?,
        isFavorite: Boolean?,
        minHits: Int?,
        maxHits: Int?
    ): Flow<List<WordWithCount>> {
        return wordDao.searchWordsWithCount(query, bookId, author, isFavorite, minHits, maxHits).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllWordsWithCount(): Flow<List<WordWithCount>> {
        return wordDao.getAllWordsWithCount().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFocusWords(): Flow<List<WordWithCount>> {
        return wordDao.getFocusWordsWithCount().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateFocusStatus(wordId: Long, isFocus: Boolean) {
        wordDao.updateFocusStatus(wordId, isFocus)
    }

    override fun getTotalWordsCount(): Flow<Int> {
        return wordDao.getTotalWordsCount()
    }

    override fun getUniqueWordsCountSince(timestamp: Long): Flow<Int> {
        return occurrenceDao.getUniqueWordsCountSince(timestamp)
    }

    override suspend fun updateTranslation(wordId: Long, translation: String?, status: TranslationStatus) {
        wordDao.updateTranslation(wordId, translation, status.name)
    }

    override fun getPendingTranslations(): Flow<List<Word>> {
        return wordDao.getPendingTranslations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDueWords(now: Long): Flow<List<ReviewWord>> {
        return wordDao.getDueWords(now).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun markWordReviewed(wordId: Long, timestamp: Long) {
        wordDao.markReviewed(wordId, timestamp)
    }

    override suspend fun markWordNotRemembered(wordId: Long) {
        wordDao.markNotRemembered(wordId)
    }

    override suspend fun updateSrsMetadata(
        wordId: Long,
        nextReviewAt: Instant,
        lastReviewAt: Instant,
        reviewCount: Int,
        successfulReviews: Int,
        currentIntervalDays: Int
    ) {
        wordDao.updateSrsMetadata(
            wordId,
            nextReviewAt.toEpochMilli(),
            lastReviewAt.toEpochMilli(),
            reviewCount,
            successfulReviews,
            currentIntervalDays
        )
    }

    override fun getTotalReviewsDoneCount(): Flow<Int> {
        return wordDao.getTotalReviewsDoneCount()
    }

    override fun getTotalSuccessfulReviewsCount(): Flow<Int> {
        return wordDao.getTotalSuccessfulReviewsCount()
    }

    override fun getTotalReviewAttemptsCount(): Flow<Int> {
        return wordDao.getTotalReviewAttemptsCount()
    }

    override fun getTotalOccurrencesCount(): Flow<Int> {
        return occurrenceDao.getTotalOccurrencesCount()
    }

    override fun getTranslatedWordsCount(): Flow<Int> {
        return wordDao.getTranslatedWordsCount()
    }

    override fun getTopWordsForBook(bookId: Long, limit: Int): Flow<List<WordWithCount>> {
        return occurrenceDao.getTopWordsForBook(bookId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTopWordsForChapter(chapterId: Long, limit: Int): Flow<List<WordWithCount>> {
        return occurrenceDao.getTopWordsForChapter(chapterId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getWordsForBook(bookId: Long): Flow<List<WordWithCount>> {
        return occurrenceDao.getWordsForBook(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookContributions(): Flow<List<BookContribution>> {
        return occurrenceDao.getBookContributions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChapterDifficulties(): Flow<List<ChapterDifficulty>> {
        return occurrenceDao.getChapterDifficulties().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDailyActivity(): Flow<Map<LocalDate, Int>> {
        return occurrenceDao.getDailyActivity().map { entities ->
            entities.associate { LocalDate.parse(it.date) to it.count }
        }
    }

    override fun getWordsByHitsRangeCount(min: Int, max: Int): Flow<Int> {
        return wordDao.getWordsByHitsRangeCount(min, max)
    }

    override fun getWordsAboveHitsCount(min: Int): Flow<Int> {
        return wordDao.getWordsAboveHitsCount(min)
    }

    override fun getForgottenWordsCount(): Flow<Int> {
        return wordDao.getForgottenWordsCount()
    }

    override fun getAuthorStats(): Flow<List<AuthorStats>> {
        return wordDao.getAuthorVocabularyStats().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllAuthors(): Flow<List<String>> {
        return wordDao.getAllAuthors()
    }

    override fun getWordDiscoveries(): Flow<List<WordDiscovery>> {
        return occurrenceDao.getFirstEncounters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChapterMastery(bookId: Long): Flow<Map<Long, ChapterMastery>> {
        return occurrenceDao.getChapterMasteryByBook(bookId).map { entities ->
            entities.associate { it.chapterId to it.toDomain() }
        }
    }

    override fun getTotalMasteredChaptersCount(): Flow<Int> {
        return wordDao.getTotalMasteredChaptersCount()
    }

    override suspend fun getRandomWord(): Word? {
        return wordDao.getRandomWord()?.toDomain()
    }

    override suspend fun addRelationship(wordId: Long, relatedId: Long, type: RelationshipType) {
        // Implementation TODO
    }

    override suspend fun deleteRelationship(wordId: Long, relatedId: Long, type: RelationshipType) {
        // Implementation TODO
    }

    override fun getRelatedWords(wordId: Long): Flow<List<RelatedWord>> {
        // Implementation TODO
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
}
