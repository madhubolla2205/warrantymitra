package com.warrantymitra.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.warrantymitra.data.AppDatabase
import com.warrantymitra.data.toReminderSubject
import com.warrantymitra.domain.ReminderPlanner
import com.warrantymitra.domain.formatDate
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Runs once a day around 9 AM and posts any warranty, plan or AMC reminders due today. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val items = AppDatabase.get(applicationContext).items().getAll()
        ReminderPlanner.due(items.map { it.toReminderSubject() }, LocalDate.now(), ::formatDate)
            .forEachIndexed { i, r -> Notifications.post(applicationContext, r, i) }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily-warranty-reminders"

        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(9, 0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, next).toMinutes(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
