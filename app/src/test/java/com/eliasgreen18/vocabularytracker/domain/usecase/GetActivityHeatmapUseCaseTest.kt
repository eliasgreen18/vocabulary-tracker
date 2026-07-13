package com.eliasgreen18.vocabularytracker.domain.usecase

import app.cash.turbine.test
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetActivityHeatmapUseCaseTest {

    private lateinit var wordRepository: WordRepository
    private lateinit var getActivityHeatmapUseCase: GetActivityHeatmapUseCase

    @Before
    fun setUp() {
        wordRepository = mockk()
        getActivityHeatmapUseCase = GetActivityHeatmapUseCase(wordRepository)
    }

    @Test
    fun `when activity has gaps, current streak should only count consecutive days from today or yesterday`() = runTest {
        // Given
        val today = LocalDate.now()
        val activityMap = mapOf(
            today to 5,
            today.minusDays(1) to 3,
            today.minusDays(3) to 2 // Gap at minusDays(2)
        )
        every { wordRepository.getDailyActivity() } returns flowOf(activityMap)

        // When & Then
        getActivityHeatmapUseCase().test {
            val result = awaitItem()
            assertEquals(2, result.streakInfo.currentStreak)
            assertEquals(2, result.streakInfo.longestStreak)
            awaitComplete()
        }
    }

    @Test
    fun `when activity is old, current streak should be zero`() = runTest {
        // Given
        val today = LocalDate.now()
        val activityMap = mapOf(
            today.minusDays(2) to 5,
            today.minusDays(3) to 3
        )
        every { wordRepository.getDailyActivity() } returns flowOf(activityMap)

        // When & Then
        getActivityHeatmapUseCase().test {
            val result = awaitItem()
            assertEquals(0, result.streakInfo.currentStreak)
            assertEquals(2, result.streakInfo.longestStreak)
            awaitComplete()
        }
    }

    @Test
    fun `when activity is consecutive, longest streak should reflect the total run`() = runTest {
        // Given
        val today = LocalDate.now()
        val activityMap = (0..10).associate { today.minusDays(it.toLong()) to 1 }
        every { wordRepository.getDailyActivity() } returns flowOf(activityMap)

        // When & Then
        getActivityHeatmapUseCase().test {
            val result = awaitItem()
            assertEquals(11, result.streakInfo.currentStreak)
            assertEquals(11, result.streakInfo.longestStreak)
            awaitComplete()
        }
    }

    @Test
    fun `when activity map is empty, streaks should be zero`() = runTest {
        // Given
        every { wordRepository.getDailyActivity() } returns flowOf(emptyMap())

        // When & Then
        getActivityHeatmapUseCase().test {
            val result = awaitItem()
            assertEquals(0, result.streakInfo.currentStreak)
            assertEquals(0, result.streakInfo.longestStreak)
            awaitComplete()
        }
    }
}
