package com.raven.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raven.app.util.RavenNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: RavenNotificationManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_TITLE = "title"
        const val KEY_DESCRIPTION = "description"
    }

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val description = inputData.getString(KEY_DESCRIPTION) ?: ""

        if (reminderId == -1L) return Result.failure()

        notificationManager.showReminderNotification(
            id = reminderId.toInt(),
            title = title,
            description = description
        )

        return Result.success()
    }
}
