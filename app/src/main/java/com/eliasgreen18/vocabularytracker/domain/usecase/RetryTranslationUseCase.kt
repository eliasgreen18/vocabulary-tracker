package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.Word
import javax.inject.Inject

class RetryTranslationUseCase @Inject constructor(
    private val requestTranslationUseCase: RequestTranslationUseCase
) {
    suspend operator fun invoke(word: Word) {
        requestTranslationUseCase(word.id, word.text)
    }
}
