package com.eliasgreen18.vocabularytracker.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiTutorService @Inject constructor() {

    suspend fun getWordInsights(word: String, apiKey: String): Result<AiInsights> = withContext(Dispatchers.IO) {
        // We will try 'gemini-1.5-flash-latest' which is the most widely compatible stable ID
        // and we will force JSON response mode if the SDK supports it.
        val modelName = "gemini-1.5-flash"
        
        try {
            Log.d("AiTutorService", "Hard-Reset attempt with model: $modelName")
            
            val generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )

            val prompt = """
                Eres un tutor de inglés. Explica '$word' para un hispanohablante.
                Responde estrictamente en JSON con estas llaves: "explanation" y "examples".
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: return@withContext Result.failure(Exception("Empty AI body"))

            Log.d("AiTutorService", "JSON Mode Success. Response: $text")

            val explanation = extractJsonValue(text, "explanation")
            val examples = extractJsonValue(text, "examples")

            if (explanation == "No explanation available") {
                Result.failure(Exception("Field missing in JSON: $text"))
            } else {
                Result.success(AiInsights(explanation, examples))
            }
        } catch (e: Exception) {
            Log.e("AiTutorService", "Final fallback attempt...", e)
            // Try one more time without JSON mode just in case
            try {
                val legacyModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
                val legacyResponse = legacyModel.generateContent("Explica la palabra '$word' y da 3 ejemplos. Formato JSON: {explanation, examples}")
                val legacyText = legacyResponse.text ?: throw e
                
                val start = legacyText.indexOf('{')
                val end = legacyText.lastIndexOf('}')
                if (start != -1 && end != -1) {
                    val json = legacyText.substring(start, end + 1)
                    Result.success(AiInsights(extractJsonValue(json, "explanation"), extractJsonValue(json, "examples")))
                } else {
                    throw e
                }
            } catch (e2: Exception) {
                // Return the most descriptive error
                val msg = e2.message ?: "Unknown AI error"
                Result.failure(Exception("Critical Error 404/Not Found. Possible solution: Check if your API Key has 'Gemini API' enabled in Google Cloud Console or try a new key from a @gmail.com account."))
            }
        }
    }

    private fun extractJsonValue(text: String, key: String): String {
        val regex = "\"$key\":\\s*\"(.*?)\"(?:,|\\s*})".toRegex(RegexOption.DOT_MATCHES_ALL)
        return regex.find(text)?.groupValues?.get(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.trim() ?: "No $key available"
    }
}

data class AiInsights(
    val explanation: String,
    val examples: String
)
