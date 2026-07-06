package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProcessPendingTranslationsUseCase @Inject constructor(
    private val repository: WordRepository,
    private val requestTranslationUseCase: RequestTranslationUseCase
) {
    suspend operator fun invoke() {
        val pendingWords = repository.getPendingTranslations().first()
        pendingWords.forEach { word ->
            // Reset status to ERROR if stuck in LOADING/PENDING to allow retry logic in RequestTranslationUseCase
            // This handles app crashes during active translation
            if (word.translationStatus == TranslationStatus.LOADING || word.translationStatus == TranslationStatus.PENDING) {
                // We force a retry by making it eligible for RequestTranslationUseCase
                // Actually RequestTranslationUseCase now accepts NOT_REQUESTED or ERROR. 
                // Let's explicitly trigger them.
                val eligibleWord = if (word.translationStatus != TranslationStatus.ERROR) {
                    word.copy(translationStatus = TranslationStatus.ERROR) 
                } else word
                
                requestTranslationUseCase(eligibleWord)
            } else if (word.translationStatus == TranslationStatus.ERROR) {
                requestTranslationUseCase(word)
            }
        }
    }
}
