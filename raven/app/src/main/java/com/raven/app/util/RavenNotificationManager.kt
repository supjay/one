package com.raven.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.raven.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RavenNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_REMINDERS = "raven_reminders"
        const val CHANNEL_FAMILY = "raven_family"
        const val CHANNEL_GENERAL = "raven_general"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminder notifications" },
            NotificationChannel(
                CHANNEL_FAMILY,
                "Family Commitments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Family event notifications" },
            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General Raven notifications" }
        )
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    fun showReminderNotification(id: Int, title: String, description: String) {
        val mainActivityClass = try {
            Class.forName("com.raven.app.presentation.main.MainActivity")
        } catch (_: ClassNotFoundException) { return }

        val intent = Intent(context, mainActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "reminders")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description.ifBlank { "Tap to view" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }
}
