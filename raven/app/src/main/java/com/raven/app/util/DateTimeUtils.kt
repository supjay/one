package com.raven.app.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    private val dateTimeFormat = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))
    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))
    fun formatShortDate(millis: Long): String = shortDateFormat.format(Date(millis))
    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))
    fun formatMonthYear(millis: Long): String = monthYearFormat.format(Date(millis))

    fun formatRelative(millis: Long): String {
        val now = System.currentTimeMillis()
        val diff = millis - now
        val absDiff = Math.abs(diff)
        val isPast = diff < 0

        return when {
            absDiff < TimeUnit.MINUTES.toMillis(1) -> if (isPast) "Just now" else "In a moment"
            absDiff < TimeUnit.HOURS.toMillis(1) -> {
                val mins = TimeUnit.MILLISECONDS.toMinutes(absDiff)
                if (isPast) "$mins min ago" else "In $mins min"
            }
            absDiff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(absDiff)
                if (isPast) "$hours hr ago" else "In $hours hr"
            }
            absDiff < TimeUnit.DAYS.toMillis(2) -> if (isPast) "Yesterday" else "Tomorrow"
            absDiff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(absDiff)
                if (isPast) "$days days ago" else "In $days days"
            }
            else -> formatDate(millis)
        }
    }

    fun startOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun today(): Long = System.currentTimeMillis()
    fun tomorrow(): Long = today() + TimeUnit.DAYS.toMillis(1)
    fun nextWeek(): Long = today() + TimeUnit.DAYS.toMillis(7)
}
