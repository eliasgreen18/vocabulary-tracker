package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Occurrence
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import java.time.Instant
import javax.inject.Inject

class RecordOccurrenceUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, sessionId: Long) {
        val occurrence = Occurrence(
            wordId = wordId,
            sessionId = sessionId,
            createdAt = Instant.now()
        )
        repository.insertOccurrence(occurrence)
    }
}
