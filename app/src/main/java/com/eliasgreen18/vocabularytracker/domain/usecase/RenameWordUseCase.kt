package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class RenameWordUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, newText: String) {
        val normalized = newText.trim().lowercase()
        if (normalized.isNotBlank()) {
            repository.updateWordText(wordId, normalized)
        }
    }
}
