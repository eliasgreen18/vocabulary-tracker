package com.eliasgreen18.vocabularytracker.data.remote.drive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var driveService: Drive? = null

    fun setupService(accountName: String) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
        ).setSelectedAccountName(accountName)

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Vocabulary Tracker").build()
    }

    suspend fun uploadDatabase(dbFile: java.io.File): Result<String> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext Result.failure(Exception("Drive service not initialized"))
        
        try {
            // 1. Find ALL existing files with this name in appDataFolder (including hidden/duplicates)
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'vocabulary_latest.db'")
                .setFields("files(id, name)")
                .execute()
            
            val existingFiles = result.files

            // 2. Aggressive Cleanup: Delete ALL existing instances before creating a new one
            // This bypasses the versioning issues in the update() API
            if (!existingFiles.isNullOrEmpty()) {
                for (file in existingFiles) {
                    try {
                        service.files().delete(file.id).execute()
                    } catch (e: Exception) {
                        // Log or ignore individual delete failures
                    }
                }
            }

            // 3. Create fresh file
            val fileMetadata = File().apply {
                name = "vocabulary_latest.db"
                parents = listOf("appDataFolder")
            }
            val mediaContent = FileContent("application/octet-stream", dbFile)

            val newFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            Result.success(newFile.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
