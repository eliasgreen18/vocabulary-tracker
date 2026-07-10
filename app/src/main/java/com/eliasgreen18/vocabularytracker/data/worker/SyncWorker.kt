package com.eliasgreen18.vocabularytracker.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eliasgreen18.vocabularytracker.data.remote.drive.GoogleDriveService
import com.eliasgreen18.vocabularytracker.data.util.BackupService
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val googleDriveService: GoogleDriveService,
    private val backupService: BackupService,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Check if auto-sync is enabled
        // TODO: Map to actual account from preferences
        val accountName = "user@example.com" 
        googleDriveService.setupService(accountName)

        // 2. Export temporary DB file
        val backupResult = backupService.exportBackup()
        val dbFile = backupResult.getOrNull() ?: return Result.failure()

        // 3. Upload to Drive
        val uploadResult = googleDriveService.uploadDatabase(dbFile)

        return if (uploadResult.isSuccess) {
            Result.success()
        } else {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
