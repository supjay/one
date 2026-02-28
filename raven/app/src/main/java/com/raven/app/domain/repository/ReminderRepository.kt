package com.raven.app.domain.repository

import com.raven.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getActiveReminders(): Flow<List<Reminder>>
    fun getRemindersByDateRange(startMillis: Long, endMillis: Long): Flow<List<Reminder>>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun saveReminder(reminder: Reminder): Long
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun setCompleted(id: Long, completed: Boolean)
    suspend fun deleteCompletedReminders()
}
