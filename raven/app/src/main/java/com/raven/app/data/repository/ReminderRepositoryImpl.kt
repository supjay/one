package com.raven.app.data.repository

import com.raven.app.data.local.dao.ReminderDao
import com.raven.app.data.local.entities.ReminderEntity
import com.raven.app.domain.model.Priority
import com.raven.app.domain.model.Reminder
import com.raven.app.domain.model.RepeatMode
import com.raven.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> =
        dao.getAllReminders().map { list -> list.map { it.toDomain() } }

    override fun getActiveReminders(): Flow<List<Reminder>> =
        dao.getActiveReminders().map { list -> list.map { it.toDomain() } }

    override fun getRemindersByDateRange(startMillis: Long, endMillis: Long): Flow<List<Reminder>> =
        dao.getRemindersByDateRange(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun getReminderById(id: Long): Reminder? =
        dao.getReminderById(id)?.toDomain()

    override suspend fun saveReminder(reminder: Reminder): Long =
        dao.insertReminder(reminder.toEntity())

    override suspend fun deleteReminder(reminder: Reminder) =
        dao.deleteReminder(reminder.toEntity())

    override suspend fun setCompleted(id: Long, completed: Boolean) =
        dao.setReminderCompleted(id, completed)

    override suspend fun deleteCompletedReminders() =
        dao.deleteCompletedReminders()

    private fun ReminderEntity.toDomain() = Reminder(
        id = id,
        title = title,
        description = description,
        dateTimeMillis = dateTimeMillis,
        isCompleted = isCompleted,
        priority = Priority.fromValue(priority),
        repeatMode = RepeatMode.valueOf(repeatMode),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Reminder.toEntity() = ReminderEntity(
        id = id,
        title = title,
        description = description,
        dateTimeMillis = dateTimeMillis,
        isCompleted = isCompleted,
        priority = priority.value,
        repeatMode = repeatMode.name,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
