package com.eliasgreen18.vocabularytracker.domain.repository

interface TranslationService {
    suspend fun translate(
        text: String, 
        sourceLang: String = "en", 
        targetLang: String = "es"
    ): Result<String>
}
