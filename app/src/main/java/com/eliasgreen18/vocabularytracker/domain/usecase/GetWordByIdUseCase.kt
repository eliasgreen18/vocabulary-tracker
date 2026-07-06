package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class GetWordByIdUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(id: Long): Word? {
        return repository.getWordById(id)
    }
}
