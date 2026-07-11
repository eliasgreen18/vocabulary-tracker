package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.PhoneticService
import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterWordUseCase @Inject constructor(
    private val repository: WordRepository,
    private val findWordUseCase: FindWordUseCase,
    private val recordOccurrenceUseCase: RecordOccurrenceUseCase,
    private val translationService: TranslationService,
    private val phoneticService: PhoneticService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

        // 1. Record the occurrence
        recordOccurrenceUseCase(wordId, sessionId, snippet)
        
        // 2. Fetch total count to check threshold
        val totalCount = repository.getOccurrenceCountSync(wordId)
        
        // 3. Magic Auto-Fill only if threshold reached (e.g. 3rd encounter)
        // OR if it's already a "Focus Word"
        if (totalCount >= 3 || word.isFocusWord) {
            if (word.translation.isNullOrBlank() || word.ipa.isNullOrBlank()) {
                scope.launch {
                    autoFillMetadata(wordId, normalizedText)
                }
            }
        }
    }

    private suspend fun autoFillMetadata(wordId: Long, text: String) {
        try {
            repository.updateTranslation(wordId, null, TranslationStatus.LOADING)
            
            val translationResult = translationService.translate(text)
            val translation = translationResult.getOrNull()
            val ipa = phoneticService.getIpa(text)
            
            if (translation != null) {
                repository.updateTranslation(wordId, translation, TranslationStatus.DONE)
            } else {
                repository.updateTranslation(wordId, null, TranslationStatus.ERROR)
            }
            
            if (ipa != null) {
                repository.updateIpa(wordId, ipa)
            }
        } catch (e: Exception) {
            repository.updateTranslation(wordId, null, TranslationStatus.ERROR)
        }
    }
}
