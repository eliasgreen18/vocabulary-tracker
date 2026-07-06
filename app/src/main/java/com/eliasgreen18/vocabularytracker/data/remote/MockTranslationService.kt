package com.eliasgreen18.vocabularytracker.data.remote

import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class MockTranslationService @Inject constructor() : TranslationService {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> {
        delay(2.seconds) // Simulate network delay
        
        // Mock dictionary for demo purposes
        val mockTranslations = mapOf(
            "gleam" to "resplandor / brillo tenue",
            "moor" to "páramo / amarrar",
            "arcane" to "arcano / misterioso",
            "shimmer" to "brillo trémulo",
            "gloomy" to "sombrío",
            "wilderness" to "desierto / naturaleza salvaje"
        )
        
        val translation = mockTranslations[text.lowercase()] ?: "Traducción de '$text' ($sourceLang -> $targetLang)"
        return Result.success(translation)
    }
}
