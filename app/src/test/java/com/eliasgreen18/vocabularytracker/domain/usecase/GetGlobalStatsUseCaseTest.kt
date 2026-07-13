package com.eliasgreen18.vocabularytracker.domain.usecase

import app.cash.turbine.test
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGlobalStatsUseCaseTest {

    private lateinit var wordRepository: WordRepository
    private lateinit var chapterRepository: ChapterRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var sessionRepository: ReadingSessionRepository
    private lateinit var getGlobalStatsUseCase: GetGlobalStatsUseCase

    @Before
    fun setUp() {
        wordRepository = mockk()
        chapterRepository = mockk()
        bookRepository = mockk()
        sessionRepository = mockk()
        getGlobalStatsUseCase = GetGlobalStatsUseCase(
            wordRepository,
            chapterRepository,
            bookRepository,
            sessionRepository
        )
    }

    @Test
    fun `when invoke, should combine all repository flows into GlobalStats`() = runTest {
        // Given
        val words = listOf(
            mockWord(1),  // NEW
            mockWord(3),  // LEARNING
            mockWord(10)  // LEARNED
        )
        val books = listOf(
            mockBook(BookStatus.FINISHED),
            mockBook(BookStatus.READING)
        )

        every { wordRepository.searchWords("") } returns flowOf(words)
        every { wordRepository.getTotalOccurrencesCount() } returns flowOf(10)
        every { wordRepository.getTranslatedWordsCount() } returns flowOf(5)
        every { wordRepository.getTotalReviewsDoneCount() } returns flowOf(3)
        every { wordRepository.getTotalSuccessfulReviewsCount() } returns flowOf(2)
        every { wordRepository.getTotalReviewAttemptsCount() } returns flowOf(4)
        every { chapterRepository.getTotalChaptersCount() } returns flowOf(8)
        every { bookRepository.getAllBooks() } returns flowOf(books)
        every { sessionRepository.getTotalReadingTimeSeconds() } returns flowOf(3600L)

        // When & Then
        getGlobalStatsUseCase().test {
            val stats = awaitItem()
            assertEquals(3, stats.uniqueWordsCount)
            assertEquals(10, stats.totalOccurrencesCount)
            assertEquals(1, stats.newWordsCount)
            assertEquals(1, stats.learningWordsCount)
            assertEquals(1, stats.learnedWordsCount)
            assertEquals(5, stats.translatedWordsCount)
            assertEquals(8, stats.totalChaptersCount)
            assertEquals(1, stats.completedBooksCount)
            assertEquals(3600L, stats.totalReadingTimeSeconds)
            assertEquals(3, stats.totalReviewsDone)
            assertEquals(2, stats.successfulReviews)
            assertEquals(4, stats.totalReviewAttempts)
            awaitComplete()
        }
    }

    private fun mockWord(hits: Int) = WordWithCount(
        wordId = 0,
        wordText = "test",
        sessionCount = 0,
        globalCount = hits,
        isFocusWord = false,
        translationStatus = TranslationStatus.NOT_REQUESTED
    )

    private fun mockBook(status: BookStatus) = Book(
        id = 0,
        title = "Test",
        author = "Author",
        language = "EN",
        status = status
    )
}
