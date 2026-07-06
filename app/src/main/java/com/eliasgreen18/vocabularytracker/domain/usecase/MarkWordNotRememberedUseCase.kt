package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class MarkWordNotRememberedUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long) {
        repository.markWordNotRemembered(wordId)
    }
}
