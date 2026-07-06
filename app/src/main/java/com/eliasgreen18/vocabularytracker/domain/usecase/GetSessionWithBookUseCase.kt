package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ActiveSessionInfo
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionWithBookUseCase @Inject constructor(
    private val sessionRepository: ReadingSessionRepository
) {
    operator fun invoke(sessionId: Long): Flow<ActiveSessionInfo?> {
        return sessionRepository.getSessionWithDetailsById(sessionId)
    }
}
