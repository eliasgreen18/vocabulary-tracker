package com.eliasgreen18.vocabularytracker.domain.usecase

import android.content.Context
import androidx.work.*
import com.eliasgreen18.vocabularytracker.data.worker.TranslationWorker
import com.eliasgreen18.vocabularytracker.domain.model.TranslationStatus
import com.eliasgreen18.vocabularytracker.domain.model.Word
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RequestTranslationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: WordRepository
) {
    suspend operator fun invoke(word: Word) {
        // Idempotency: only allow if not already in progress or done
        if (word.translationStatus !in listOf(TranslationStatus.NOT_REQUESTED, TranslationStatus.ERROR)) {
            return
        }

        // Mark as PENDING in DB immediately to prevent duplicate enqueuing
        repository.updateTranslation(word.id, null, TranslationStatus.PENDING)

        val workRequest = OneTimeWorkRequestBuilder<TranslationWorker>()
            .setInputData(
                workDataOf(
                    "wordId" to word.id,
                    "text" to word.text
                )
            )
            // Removed Network Constraint for Local Dictionary speed & offline use
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        // Unique work name ensures only one job runs per wordId
        WorkManager.getInstance(context).enqueueUniqueWork(
            "translate_${word.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
