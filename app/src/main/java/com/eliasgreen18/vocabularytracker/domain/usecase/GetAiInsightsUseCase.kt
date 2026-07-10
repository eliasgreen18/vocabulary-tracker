package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.data.remote.AiTutorService
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAiInsightsUseCase @Inject constructor(
    private val aiTutorService: AiTutorService,
    private val repository: WordRepository,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(wordId: Long): Result<Unit> {
        val word = repository.getWordById(wordId) ?: return Result.failure(Exception("Word not found"))
        val apiKey = preferencesRepository.getGeminiApiKey().first() 
            ?: return Result.failure(Exception("Please add your Gemini API Key in Settings"))

        return aiTutorService.getWordInsights(word.text, apiKey).fold(
            onSuccess = { insights ->
                repository.updateAiInsights(wordId, insights.explanation, insights.examples)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
