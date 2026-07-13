package com.eliasgreen18.vocabularytracker.domain.usecase

import android.content.Context
import androidx.work.*
import com.eliasgreen18.vocabularytracker.data.worker.TranslationWorker
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RequestTranslationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, text: String) {
        // Mark as PENDING in DB immediately to prevent duplicate enqueuing
        repository.updateTranslation(wordId, null, TranslationStatus.PENDING)

        val workRequest = OneTimeWorkRequestBuilder<TranslationWorker>()
            .setInputData(
                workDataOf(
                    "wordId" to wordId,
                    "text" to text
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "translate_$wordId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
