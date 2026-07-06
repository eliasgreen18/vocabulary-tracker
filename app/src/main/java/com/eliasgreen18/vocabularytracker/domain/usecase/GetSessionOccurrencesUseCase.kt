package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionOccurrencesUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(sessionId: Long): Flow<List<WordWithCount>> {
        return repository.getSessionWords(sessionId)
    }
}
