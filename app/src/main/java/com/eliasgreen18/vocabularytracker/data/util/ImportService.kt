package com.eliasgreen18.vocabularytracker.data.util

import android.content.Context
import android.net.Uri
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend fun importFromJson(uri: Uri): Result<List<WordWithCount>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open file"))
            
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<WordWithCount>>() {}.type
            val words: List<WordWithCount> = gson.fromJson(reader, type)
            
            inputStream.close()
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromCsv(uri: Uri): Result<List<WordWithCount>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open file"))
            
            val reader = BufferedReader(InputStreamReader(inputStream))
            val words = mutableListOf<WordWithCount>()
            
            // Skip header
            reader.readLine()
            
            var line: String? = reader.readLine()
            while (line != null) {
                val parts = parseCsvLine(line)
                if (parts.size >= 2) {
                    val wordText = parts[0].trim()
                    val translation = parts.getOrNull(1)?.trim()?.ifBlank { null }
                    val globalCount = parts.getOrNull(2)?.toIntOrNull() ?: 1
                    
                    words.add(
                        WordWithCount(
                            wordId = 0, // ID will be assigned by DB or used for matching
                            wordText = wordText,
                            sessionCount = 0,
                            globalCount = globalCount,
                            isFocusWord = false,
                            translation = translation,
                            translationStatus = if (translation != null) TranslationStatus.DONE else TranslationStatus.NOT_REQUESTED
                        )
                    )
                }
                line = reader.readLine()
            }
            
            inputStream.close()
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var currentPart = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(currentPart.toString())
                    currentPart = StringBuilder()
                }
                else -> currentPart.append(char)
            }
        }
        result.add(currentPart.toString())
        return result
    }
}
