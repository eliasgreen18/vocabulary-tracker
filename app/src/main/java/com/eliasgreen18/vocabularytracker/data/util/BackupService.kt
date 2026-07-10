package com.eliasgreen18.vocabularytracker.data.util

import android.content.Context
import com.eliasgreen18.vocabularytracker.data.local.db.VocabularyDatabase
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: VocabularyDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend fun exportBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            // 1. Force a checkpoint to merge WAL data into the main .db file
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath("vocabulary_db")
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            // 2. Prepare destination file name based on preferences
            val keepHistory = preferencesRepository.getKeepBackupHistory().first()
            val backupFileName = if (keepHistory) {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                "vocabulary_backup_$timestamp.db"
            } else {
                "vocabulary_latest.db"
            }

            val backupFile = File(context.cacheDir, backupFileName)
            
            // Delete if exists and not keeping history (though FileOutputStream would overwrite, 
            // explicit delete is cleaner for cache management)
            if (!keepHistory && backupFile.exists()) {
                backupFile.delete()
            }

            // 3. Copy file
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
