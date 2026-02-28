package com.raven.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long,
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 0=Low, 1=Medium, 2=High
    val repeatMode: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
