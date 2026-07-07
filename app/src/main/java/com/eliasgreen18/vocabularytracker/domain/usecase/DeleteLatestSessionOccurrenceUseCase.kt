package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class DeleteLatestSessionOccurrenceUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, sessionId: Long) {
        repository.deleteLatestOccurrenceInSession(wordId, sessionId)
    }
}
