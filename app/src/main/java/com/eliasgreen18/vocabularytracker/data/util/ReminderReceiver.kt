package com.eliasgreen18.vocabularytracker.data.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.eliasgreen18.vocabularytracker.data.worker.NotificationWorker
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ReminderReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderEntryPoint {
        fun preferencesRepository(): UserPreferencesRepository
        fun notificationScheduler(): NotificationScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule after reboot
            val entryPoint = EntryPointAccessors.fromApplication(context, ReminderEntryPoint::class.java)
            val prefs = entryPoint.preferencesRepository()
            val scheduler = entryPoint.notificationScheduler()

            runBlocking {
                if (prefs.isNotificationEnabled().first()) {
                    val time = prefs.getNotificationTime().first()
                    scheduler.scheduleDailyNotification(time.first, time.second)
                }
            }
            return
        }

        // Trigger the actual notification logic via WorkManager
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
