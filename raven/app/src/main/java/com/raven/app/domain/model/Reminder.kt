package com.raven.app.domain.model

data class Reminder(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Priority(val label: String, val value: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2);

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}

enum class RepeatMode(val label: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}
