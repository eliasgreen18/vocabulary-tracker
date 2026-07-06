package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class SaveManualTranslationUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, translation: String) {
        if (translation.isNotBlank()) {
            repository.updateTranslation(wordId, translation.trim(), TranslationStatus.DONE)
        }
    }
}
