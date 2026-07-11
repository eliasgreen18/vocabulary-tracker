package com.eliasgreen18.vocabularytracker.data.remote.drive

import android.content.Context
import android.util.Log
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
            context, listOf(DriveScopes.DRIVE_FILE)
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
            Log.d("GoogleDriveService", "Starting upload to Google Drive...")
            
            val result = service.files().list()
                .setQ("name = 'vocabulary_latest.db' and trashed = false")
                .setFields("files(id, name)")
                .execute()
            
            val existingFiles = result.files

            if (!existingFiles.isNullOrEmpty()) {
                for (file in existingFiles) {
                    try {
                        service.files().delete(file.id).execute()
                    } catch (e: Exception) {
                        Log.w("GoogleDriveService", "Could not delete old file: ${e.message}")
                    }
                }
            }

            val fileMetadata = File().apply {
                name = "vocabulary_latest.db"
            }
            val mediaContent = FileContent("application/octet-stream", dbFile)

            val newFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            Log.d("GoogleDriveService", "Upload successful!")
            Result.success(newFile.id)
        } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
            Result.failure(Exception("Google needs additional permission. Please disconnect and reconnect in Settings."))
        } catch (e: Exception) {
            Log.e("GoogleDriveService", "Upload failed", e)
            val detailedError = e.message ?: e.toString()
            Result.failure(Exception(detailedError))
        }
    }
}
