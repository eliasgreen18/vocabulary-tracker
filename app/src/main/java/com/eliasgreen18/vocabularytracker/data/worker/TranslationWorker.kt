package com.eliasgreen18.vocabularytracker.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TranslationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: WordRepository,
    private val translationService: TranslationService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val wordId = inputData.getLong("wordId", -1L)
        val text = inputData.getString("text") ?: return Result.failure()

        if (wordId == -1L) return Result.failure()

        // Transition PENDING -> LOADING
        repository.updateTranslation(wordId, null, TranslationStatus.LOADING)

        val result = translationService.translate(text)

        return if (result.isSuccess) {
            val translation = result.getOrNull()
            repository.updateTranslation(wordId, translation, TranslationStatus.DONE)
            Result.success()
        } else {
            // Check if we should retry or fail permanently
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                repository.updateTranslation(wordId, null, TranslationStatus.ERROR)
                Result.failure()
            }
        }
    }
}
