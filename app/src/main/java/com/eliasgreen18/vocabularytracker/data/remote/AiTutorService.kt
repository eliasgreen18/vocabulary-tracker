package com.eliasgreen18.vocabularytracker.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiTutorService @Inject constructor() {

    suspend fun getWordInsights(word: String, apiKey: String): Result<AiInsights> = withContext(Dispatchers.IO) {
        // Updated list using strictly the standard AI Studio model names
        val modelsToTry = listOf(
            "gemini-1.5-flash-latest",
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-pro"
        )
        
        var lastError: Exception? = null

        for (modelName in modelsToTry) {
            try {
                Log.d("AiTutorService", "Attempting connection with model: $modelName")
                
                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )

                val prompt = "Eres un tutor. Explica '$word' en español y da 2 ejemplos. Responde en JSON: {\"explanation\": \"...\", \"examples\": \"...\"}"

                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: continue

                Log.d("AiTutorService", "SUCCESS with model: $modelName")

                val explanation = extractJsonValue(text, "explanation")
                val examples = extractJsonValue(text, "examples")

                if (explanation != "No explanation available") {
                    return@withContext Result.success(AiInsights(explanation, examples))
                }
            } catch (e: Exception) {
                Log.w("AiTutorService", "Model $modelName failed: ${e.message}")
                lastError = e
            }
        }

        val finalMsg = lastError?.message ?: "No response from Gemini models"
        
        // Provide clear instructions if all fail
        val helpfulError = when {
            finalMsg.contains("404") -> "Error 404: Google no encuentra el modelo. POR FAVOR: En AI Studio (aistudio.google.com), usa el botón 'Create API key in NEW project' para generar una clave limpia."
            finalMsg.contains("403") -> "Error 403: Permiso denegado. Tu cuenta tiene restricciones. Prueba con una clave generada en un PROYECTO NUEVO de AI Studio."
            else -> "Error de IA: $finalMsg"
        }

        Result.failure(Exception(helpfulError))
    }

    private fun extractJsonValue(text: String, key: String): String {
        val regex = "\"$key\":\\s*\"(.*?)\"(?:,|\\s*})".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(text)
        
        return match?.groupValues?.get(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.trim() ?: "No $key available"
    }
}

data class AiInsights(
    val explanation: String,
    val examples: String
)
