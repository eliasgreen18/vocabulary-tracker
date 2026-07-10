package com.eliasgreen18.vocabularytracker.data.util

import android.content.Context
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToCsv(words: List<WordWithCount>): Result<File> = withContext(Dispatchers.IO) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            val fileName = "vocabulary_export_$timestamp.csv"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { output ->
                val header = "Word,Translation,Hits,Mastery\n"
                output.write(header.toByteArray())
                
                words.forEach { word ->
                    val line = "${escapeCsv(word.wordText)},${escapeCsv(word.translation ?: "")},${word.globalCount},${word.mastery.name}\n"
                    output.write(line.toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToJson(words: List<WordWithCount>): Result<File> = withContext(Dispatchers.IO) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            val fileName = "vocabulary_export_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            
            val json = gson.toJson(words)
            file.writeText(json)
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsv(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"" + text.replace("\"", "\"\"") + "\""
        } else {
            text
        }
    }
}
