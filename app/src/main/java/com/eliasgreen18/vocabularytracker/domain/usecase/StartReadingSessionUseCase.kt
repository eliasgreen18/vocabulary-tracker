package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import java.time.Instant
import javax.inject.Inject

class StartReadingSessionUseCase @Inject constructor(
    private val repository: ReadingSessionRepository
) {
    suspend operator fun invoke(chapterId: Long): Long {
        val newSession = ReadingSession(
            chapterId = chapterId,
            startedAt = Instant.now()
        )
        return repository.insertSession(newSession)
    }
}
