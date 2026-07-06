package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RegisterWordUseCase @Inject constructor(
    private val repository: WordRepository,
    private val findWordUseCase: FindWordUseCase,
    private val recordOccurrenceUseCase: RecordOccurrenceUseCase,
    private val requestTranslationUseCase: RequestTranslationUseCase
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
        
        // Check if we should trigger translation
        word?.let { w ->
            if (w.translationStatus == TranslationStatus.NOT_REQUESTED) {
                val totalCount = repository.searchWords(normalizedText).first()
                    .find { it.wordId == wordId }?.globalCount ?: 0
                
                if (totalCount >= 3) {
                    requestTranslationUseCase(w)
                }
            }
        }
    }
}
