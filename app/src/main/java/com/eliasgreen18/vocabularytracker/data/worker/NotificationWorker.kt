package com.eliasgreen18.vocabularytracker.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eliasgreen18.vocabularytracker.domain.usecase.GetDueWordsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getDueWordsUseCase: GetDueWordsUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "daily_review_channel"
        private const val NOTIFICATION_ID = 101
    }

    override suspend fun doWork(): Result {
        val dueWords = getDueWordsUseCase().first()
        val count = dueWords.size

        if (count > 0) {
            showNotification(count)
        }

        return Result.success()
    }

    private fun showNotification(count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Review Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications to remind you of words due for review"
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder icon
            .setContentTitle("Time for your Daily Review!")
            .setContentText("You have $count words ready to review today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
