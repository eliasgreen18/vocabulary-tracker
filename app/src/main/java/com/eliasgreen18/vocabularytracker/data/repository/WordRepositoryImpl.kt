package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.OccurrenceDao
import com.eliasgreen18.vocabularytracker.data.local.dao.WordDao
import com.eliasgreen18.vocabularytracker.data.local.entity.toDomain
import com.eliasgreen18.vocabularytracker.data.local.entity.toEntity
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
        return occurrenceDao.getOccurrenceCountSync(wordId)
    }

    override fun getOccurrenceCountForWord(wordId: Long): Flow<Int> {
        return occurrenceDao.getOccurrenceCountForWord(wordId)
    }

    override fun getSessionWords(sessionId: Long): Flow<List<WordWithCount>> {
        return occurrenceDao.getSessionWordsWithCounts(sessionId).map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getChapterWords(chapterId: Long): Flow<List<WordWithCount>> {
        return occurrenceDao.getChapterWordsWithCounts(chapterId).map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getWordHistory(wordId: Long): Flow<List<WordOccurrenceDetail>> {
        return occurrenceDao.getWordHistory(wordId).map { entities ->
            entities.map { 
                WordOccurrenceDetail(
                    createdAt = it.createdAt,
                    bookTitle = it.bookTitle,
                    bookLanguage = it.bookLanguage,
                    chapterNumber = it.chapterNumber,
                    chapterTitle = it.chapterTitle,
                    sessionId = it.sessionId,
                    snippet = it.snippet
                )
            }
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
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getAllWordsWithCount(): Flow<List<WordWithCount>> {
        return wordDao.getAllWordsWithCount().map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getAllWords(): Flow<List<Word>> {
        return wordDao.getAllWords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFocusWords(): Flow<List<WordWithCount>> {
        return wordDao.getFocusWordsWithCount().map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
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
            entities.map { 
                ReviewWord(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    lastReviewedAt = it.lastReviewedAt,
                    reviewPriority = it.reviewPriority,
                    lastContext = formatContext(it.lastBookTitle, it.lastChapterNumber, it.lastChapterTitle),
                    lastBookLanguage = it.lastBookLanguage,
                    lastSnippet = it.snippet,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    currentIntervalDays = it.currentIntervalDays,
                    nextReviewAt = it.nextReviewAt
                )
            }
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
        return wordDao.getTotalSuccessfulReviewsCount().map { it ?: 0 }
    }

    override fun getTotalReviewAttemptsCount(): Flow<Int> {
        return wordDao.getTotalReviewAttemptsCount().map { it ?: 0 }
    }

    override fun getTotalOccurrencesCount(): Flow<Int> {
        return occurrenceDao.getTotalOccurrencesCount()
    }

    override fun getTranslatedWordsCount(): Flow<Int> {
        return wordDao.getTranslatedWordsCount()
    }

    override fun getTopWordsForBook(bookId: Long, limit: Int): Flow<List<WordWithCount>> {
        return occurrenceDao.getTopWordsForBook(bookId, limit).map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getTopWordsForChapter(chapterId: Long, limit: Int): Flow<List<WordWithCount>> {
        return occurrenceDao.getTopWordsForChapter(chapterId, limit).map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getWordsForBook(bookId: Long): Flow<List<WordWithCount>> {
        return occurrenceDao.getWordsForBook(bookId).map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    ipa = it.ipa,
                    notes = it.notes,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getBookContributions(): Flow<List<BookContribution>> {
        return occurrenceDao.getBookContributions().map { entities ->
            entities.map { BookContribution(it.bookId, it.bookTitle, it.uniqueWordsCount, it.totalOccurrencesCount) }
        }
    }

    override fun getChapterDifficulties(): Flow<List<ChapterDifficulty>> {
        return occurrenceDao.getChapterDifficulties().map { entities ->
            entities.map { ChapterDifficulty(it.chapterId, it.bookTitle, it.chapterNumber, it.uniqueWordsCount, it.totalOccurrencesCount) }
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
            entities.map { 
                AuthorStats(
                    author = it.author,
                    uniqueWordsCount = it.uniqueWordsCount,
                    totalOccurrencesCount = it.totalOccurrencesCount
                )
            }
        }
    }

    override fun getAllAuthors(): Flow<List<String>> {
        return wordDao.getAllAuthors()
    }

    override fun getWordDiscoveries(): Flow<List<WordDiscovery>> {
        return occurrenceDao.getFirstEncounters().map { entities ->
            entities.map { 
                WordDiscovery(
                    wordText = it.wordText,
                    firstSeenAt = java.time.Instant.ofEpochMilli(it.firstSeenAt)
                )
            }
        }
    }

    override fun getChapterMastery(bookId: Long): Flow<Map<Long, ChapterMastery>> {
        return occurrenceDao.getChapterMasteryByBook(bookId).map { entities ->
            entities.associate { 
                it.chapterId to ChapterMastery(it.chapterId, it.uniqueWordsCount, it.learnedWordsCount)
            }
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

    private fun formatContext(book: String?, chapter: String?, title: String?): String {
        val b = book ?: "Unknown Book"
        val c = "Chapter $chapter"
        val t = title?.let { ": $it" } ?: ""
        return "$b - $c$t"
    }
}
