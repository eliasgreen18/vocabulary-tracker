package com.eliasgreen18.vocabularytracker.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiTutorService @Inject constructor() {

    suspend fun getWordInsights(word: String, apiKey: String): Result<AiInsights> = withContext(Dispatchers.IO) {
        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val prompt = """
                Explain the English word '$word' for a Spanish speaker learning English. 
                Provide:
                1. A simple explanation in Spanish of what it means and its nuances.
                2. Three simple example sentences in English with their Spanish translation.
                
                IMPORTANT: Return ONLY a raw JSON object. Do not use Markdown code blocks or any preamble.
                JSON format: 
                {
                  "explanation": "string",
                  "examples": "string with newlines"
                }
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: return@withContext Result.failure(Exception("Empty response from AI"))

            // Clean the response: remove potential ```json or ``` markers
            val cleanText = text.replace("```json", "").replace("```", "").trim()

            val explanation = extractJsonValue(cleanText, "explanation")
            val examples = extractJsonValue(cleanText, "examples")

            if (explanation == "No explanation available" && !cleanText.contains("explanation")) {
                return@withContext Result.failure(Exception("AI returned invalid format"))
            }

            Result.success(AiInsights(explanation, examples))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractJsonValue(text: String, key: String): String {
        // More resilient regex for different quote styles
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
