package com.eliasgreen18.vocabularytracker.ui.session

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.eliasgreen18.vocabularytracker.domain.model.*
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private val getSessionWithBookUseCase = mockk<GetSessionWithBookUseCase>()
    private val endReadingSessionUseCase = mockk<EndReadingSessionUseCase>()
    private val updateSessionDurationUseCase = mockk<UpdateSessionDurationUseCase>()
    private val upsertChapterUseCase = mockk<UpsertChapterUseCase>()
    private val registerWordUseCase = mockk<RegisterWordUseCase>()
    private val getWordsByMasteryUseCase = mockk<GetWordsByMasteryUseCase>()
    private val toggleFocusWordUseCase = mockk<ToggleFocusWordUseCase>()
    private val deleteLatestSessionOccurrenceUseCase = mockk<DeleteLatestSessionOccurrenceUseCase>()
    private val renameWordUseCase = mockk<RenameWordUseCase>()
    private val preferencesRepository = mockk<UserPreferencesRepository>()
    private val getSessionOccurrencesUseCase = mockk<GetSessionOccurrencesUseCase>()

    private lateinit var viewModel: SessionViewModel
    private val sessionId = 1L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        
        val sessionInfo = ActiveSessionInfo(
            session = ReadingSession(id = sessionId, chapterId = 10, startedAt = Instant.now(), activeDurationSeconds = 120),
            chapter = Chapter(id = 10, bookId = 1, number = "1"),
            book = null
        )

        every { getSessionWithBookUseCase(sessionId) } returns flowOf(sessionInfo)
        every { getSessionOccurrencesUseCase(sessionId) } returns flowOf(emptyList())
        every { preferencesRepository.isAutoScrollEnabled() } returns flowOf(true)
        
        viewModel = SessionViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to sessionId)),
            getSessionWithBookUseCase = getSessionWithBookUseCase,
            endReadingSessionUseCase = endReadingSessionUseCase,
            updateSessionDurationUseCase = updateSessionDurationUseCase,
            upsertChapterUseCase = upsertChapterUseCase,
            registerWordUseCase = registerWordUseCase,
            getWordsByMasteryUseCase = getWordsByMasteryUseCase,
            toggleFocusWordUseCase = toggleFocusWordUseCase,
            deleteLatestSessionOccurrenceUseCase = deleteLatestSessionOccurrenceUseCase,
            renameWordUseCase = renameWordUseCase,
            preferencesRepository = preferencesRepository,
            getSessionOccurrencesUseCase = getSessionOccurrencesUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `init should load duration from database`() = runTest {
        advanceUntilIdle()
        assertEquals(120L, viewModel.elapsedSeconds.value)
    }

    @Test
    fun `pauseTimer should save current duration to database`() = runTest {
        coEvery { updateSessionDurationUseCase(sessionId, any()) } returns Unit
        
        advanceUntilIdle() // let init finish
        viewModel.resumeTimer()
        advanceTimeBy(2.seconds)
        
        viewModel.pauseTimer()
        advanceUntilIdle()
        
        coVerify { updateSessionDurationUseCase(sessionId, 122L) }
    }
}
