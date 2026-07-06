package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import javax.inject.Inject

class UpdateReadingSessionUseCase @Inject constructor(
    private val repository: ReadingSessionRepository
) {
    suspend operator fun invoke(session: ReadingSession) {
        repository.updateSession(session)
    }
}
