package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.Word
import javax.inject.Inject

class RetryTranslationUseCase @Inject constructor(
    private val requestTranslationUseCase: RequestTranslationUseCase
) {
    suspend operator fun invoke(word: Word) {
        // Explicitly set to ERROR state if not already, to satisfy RequestTranslationUseCase condition
        val retryableWord = if (word.translationStatus != TranslationStatus.ERROR) {
            word.copy(translationStatus = TranslationStatus.ERROR)
        } else word
        
        requestTranslationUseCase(retryableWord)
    }
}
