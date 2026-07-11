package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import javax.inject.Inject

class EndReadingSessionUseCase @Inject constructor(
    private val repository: ReadingSessionRepository
) {
    suspend operator fun invoke(sessionId: Long, activeDurationSeconds: Long) {
        repository.endSession(sessionId, activeDurationSeconds)
    }
}
