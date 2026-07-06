package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReadingSession
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveSessionUseCase @Inject constructor(
    private val repository: ReadingSessionRepository
) {
    operator fun invoke(bookId: Long): Flow<ReadingSession?> {
        return repository.getActiveSessionForBook(bookId)
    }
}
