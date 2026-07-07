package com.eliasgreen18.vocabularytracker.data.repository

import com.eliasgreen18.vocabularytracker.data.local.dao.OccurrenceDao
import com.eliasgreen18.vocabularytracker.data.local.dao.WordDao
import com.eliasgreen18.vocabularytracker.data.local.entity.toDomain
import com.eliasgreen18.vocabularytracker.data.local.entity.toEntity
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
                    sessionId = it.sessionId
                )
            }
        }
    }

    override fun searchWords(query: String): Flow<List<WordWithCount>> {
        return wordDao.searchWordsWithCount(query).map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = 0,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    override fun getFocusWords(): Flow<List<WordWithCount>> {
        return wordDao.getFocusWordsWithCount().map { entities ->
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = 0,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
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
        nextReviewAt: java.time.Instant,
        lastReviewAt: java.time.Instant,
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
            entities.map { 
                WordWithCount(
                    wordId = it.wordId,
                    wordText = it.wordText,
                    sessionCount = it.sessionCount,
                    globalCount = it.globalCount,
                    isFocusWord = it.isFocusWord,
                    translation = it.translation,
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
                    translationStatus = TranslationStatus.valueOf(it.translationStatus)
                )
            }
        }
    }

    private fun formatContext(book: String?, chapterNum: Int?, chapterTitle: String?): String {
        if (book == null) return "Unknown Context"
        val chapterInfo = if (chapterTitle.isNullOrBlank()) {
            "Chapter $chapterNum"
        } else {
            "Chapter $chapterNum: $chapterTitle"
        }
        return "$book • $chapterInfo"
    }
}
