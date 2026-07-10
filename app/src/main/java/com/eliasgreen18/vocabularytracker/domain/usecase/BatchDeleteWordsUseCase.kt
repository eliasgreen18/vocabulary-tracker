package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class BatchDeleteWordsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordIds: Set<Long>) {
        wordIds.forEach { id ->
            repository.deleteWord(id)
        }
    }
}
