package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class FindWordUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(text: String): Word? {
        return repository.getWordByText(text)
    }
}
