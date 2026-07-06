package com.eliasgreen18.vocabularytracker.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LibreTranslateApi {
    @POST("translate")
    suspend fun translate(@Body request: TranslationRequest): Response<TranslationResponse>
}

data class TranslationRequest(
    val q: String,
    val source: String,
    val target: String,
    val format: String = "text"
)

data class TranslationResponse(
    val translatedText: String
)
