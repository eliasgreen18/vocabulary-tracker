package com.eliasgreen18.vocabularytracker.data.util

import android.content.Context
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun exportToAnki(words: List<WordWithCount>): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "anki_deck_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { output ->
                // Anki Header: Front, Back, Tags
                val header = "Word,Translation & Context,Tags\n"
                output.write(header.toByteArray())
                
                words.forEach { word ->
                    val front = escapeCsv(word.wordText)
                    
                    val backContent = StringBuilder()
                    word.translation?.let { backContent.append("<b>$it</b><br>") }
                    word.ipa?.let { backContent.append("<i>$it</i><br>") }
                    word.notes?.let { backContent.append("<br>Note: $it") }
                    
                    val back = escapeCsv(backContent.toString())
                    val tags = escapeCsv("vocabulary-tracker ${word.mastery.name.lowercase()}")
                    
                    output.write("$front,$back,$tags\n".toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToQuizlet(words: List<WordWithCount>): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "quizlet_list_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { output ->
                // Quizlet format: Term, Definition
                words.forEach { word ->
                    val term = escapeCsv(word.wordText)
                    val definition = escapeCsv(word.translation ?: "")
                    output.write("$term,$definition\n".toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsv(text: String): String {
        if (text.isEmpty()) return ""
        val escaped = text.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
