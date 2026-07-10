package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class RegisterWordUseCase @Inject constructor(
    private val repository: WordRepository,
    private val findWordUseCase: FindWordUseCase,
    private val recordOccurrenceUseCase: RecordOccurrenceUseCase,
    private val requestTranslationUseCase: RequestTranslationUseCase
) {
    suspend operator fun invoke(sessionId: Long, text: String, snippet: String? = null) {
        val normalizedText = text.trim().lowercase()
        if (normalizedText.isBlank()) return

        var word = findWordUseCase(normalizedText)
        val wordId = if (word == null) {
            val newWord = Word(text = normalizedText)
            val id = repository.insertWord(newWord)
            word = newWord.copy(id = id)
            id
        } else {
            word.id
        }

        // 1. Record the occurrence FIRST (with snippet)
        recordOccurrenceUseCase(wordId, sessionId, snippet)
        
        // 2. Refresh word state to get the LATEST metadata (including updated translation status if changed elsewhere)
        val updatedWord = repository.getWordById(wordId) ?: return

        // 3. Trigger translation if not done/loading and count reached threshold
        if (updatedWord.translationStatus != TranslationStatus.DONE && updatedWord.translationStatus != TranslationStatus.LOADING) {
            // Fetch the truly UPDATED count synchronously
            val totalCount = repository.getOccurrenceCountSync(wordId)
            
            if (totalCount >= 3) {
                requestTranslationUseCase(updatedWord)
            }
        }
    }
}
