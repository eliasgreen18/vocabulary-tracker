package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.WordOccurrenceDetail
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWordHistoryUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(wordId: Long): Flow<List<WordOccurrenceDetail>> {
        return repository.getWordHistory(wordId)
    }
}
