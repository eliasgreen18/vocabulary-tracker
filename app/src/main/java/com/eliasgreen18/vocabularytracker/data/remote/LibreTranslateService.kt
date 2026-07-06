package com.eliasgreen18.vocabularytracker.data.remote

import com.eliasgreen18.vocabularytracker.data.remote.api.LibreTranslateApi
import com.eliasgreen18.vocabularytracker.data.remote.api.TranslationRequest
import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import javax.inject.Inject

class LibreTranslateService @Inject constructor(
    private val api: LibreTranslateApi
) : TranslationService {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> {
        return try {
            val response = api.translate(
                TranslationRequest(
                    q = text,
                    source = sourceLang,
                    target = targetLang
                )
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.translatedText)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
