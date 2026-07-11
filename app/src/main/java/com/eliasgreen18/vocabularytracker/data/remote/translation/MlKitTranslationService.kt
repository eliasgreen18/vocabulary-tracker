package com.eliasgreen18.vocabularytracker.data.remote.translation

import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitTranslationService @Inject constructor() : TranslationService {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.SPANISH)
        .build()
        
    private val englishSpanishTranslator = Translation.getClient(options)

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> {
        return try {
            val conditions = DownloadConditions.Builder()
                .build()
            englishSpanishTranslator.downloadModelIfNeeded(conditions).await()
            
            val result = englishSpanishTranslator.translate(text).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
