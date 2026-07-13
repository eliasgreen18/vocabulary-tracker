package com.eliasgreen18.vocabularytracker.data.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveBookCover(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext Result.failure(Exception("Could not open stream"))
            
            val directory = File(context.filesDir, "covers")
            if (!directory.exists()) directory.mkdirs()
            
            val fileName = "cover_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDigitalBook(uri: Uri, extension: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext Result.failure(Exception("Could not open stream"))
            
            val directory = File(context.filesDir, "books")
            if (!directory.exists()) directory.mkdirs()
            
            val fileName = "book_${UUID.randomUUID()}.$extension"
            val file = File(directory, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getExtensionFromUri(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when (mimeType) {
            "application/epub+zip" -> "epub"
            "application/pdf" -> "pdf"
            else -> {
                // Fallback to filename extension if available
                val path = uri.path ?: ""
                if (path.lowercase().endsWith(".epub")) "epub"
                else "pdf"
            }
        }
    }

    fun deleteCover(path: String) {
        deleteFile(path)
    }

    fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log or ignore errors on delete
        }
    }
}
