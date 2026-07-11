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
            showNotification(
                title = "Time for your Daily Review!",
                text = "You have $count words ready to review today."
            )
        } else {
            showNotification(
                title = "Reading Reminder",
                text = "Time to continue your reading journey! Open your book to discover new words."
            )
        }

        return Result.success()
    }

    private fun showNotification(title: String, text: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for reviews and reading reminders"
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.eliasgreen18.vocabularytracker.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
