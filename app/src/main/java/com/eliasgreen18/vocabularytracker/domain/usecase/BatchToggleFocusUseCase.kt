package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class BatchToggleFocusUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordIds: Set<Long>, isFocus: Boolean) {
        wordIds.forEach { id ->
            repository.updateFocusStatus(id, isFocus)
        }
    }
}
