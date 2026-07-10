package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class UpdateWordMetadataUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend fun updateTranslation(wordId: Long, translation: String) {
        if (translation.isNotBlank()) {
            repository.updateTranslation(wordId, translation.trim(), TranslationStatus.DONE)
        }
    }

    suspend fun updateIpa(wordId: Long, ipa: String) {
        if (ipa.isNotBlank()) {
            repository.updateIpa(wordId, ipa.trim())
        }
    }

    suspend fun updateNotes(wordId: Long, notes: String) {
        // Notes can be empty if the user wants to clear them
        repository.updateNotes(wordId, notes.trim().ifBlank { null })
    }
}
