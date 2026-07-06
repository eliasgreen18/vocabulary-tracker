package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class RegisterWordUseCase @Inject constructor(
    private val repository: WordRepository,
    private val findWordUseCase: FindWordUseCase,
    private val recordOccurrenceUseCase: RecordOccurrenceUseCase
) {
    suspend operator fun invoke(sessionId: Long, text: String) {
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

        recordOccurrenceUseCase(wordId, sessionId)
        
        // Auto-translation disabled for stabilization
        /*
        word?.let { w ->
            if (w.translationStatus == TranslationStatus.NOT_REQUESTED) {
                val totalCount = repository.getOccurrenceCountForWord(wordId).first()
                if (totalCount >= 3) {
                    requestTranslationUseCase(w)
                }
            }
        }
        */
    }
}
