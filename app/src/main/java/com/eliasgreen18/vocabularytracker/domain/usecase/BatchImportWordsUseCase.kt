package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class BatchImportWordsUseCase @Inject constructor(
    private val repository: WordRepository,
    private val findWordUseCase: FindWordUseCase,
    private val updateWordMetadataUseCase: UpdateWordMetadataUseCase
) {
    suspend operator fun invoke(words: List<WordWithCount>, overwrite: Boolean) {
        words.forEach { imported ->
            val normalized = imported.wordText.trim().lowercase()
            if (normalized.isBlank()) return@forEach
            
            val existing = findWordUseCase(normalized)
            if (existing == null) {
                // New word
                repository.insertWord(
                    Word(
                        text = normalized,
                        translation = imported.translation,
                        translationStatus = imported.translationStatus
                    )
                )
            } else if (overwrite) {
                // Update existing
                imported.translation?.let { 
                    updateWordMetadataUseCase.updateTranslation(existing.id, it)
                }
            }
        }
    }
}
