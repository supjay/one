package com.raven.app.domain.model

data class FamilyMember(
    val id: Long = 0,
    val name: String,
    val relation: String,
    val dateOfBirthMillis: Long? = null,
    val avatarColor: String = "#6200EE",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Commitment(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long,
    val type: CommitmentType = CommitmentType.EVENT,
    val isRecurring: Boolean = false,
    val recurringPattern: String = "",
    val location: String = "",
    val isCompleted: Boolean = false,
    val reminderMillis: Long? = null,
    val members: List<FamilyMember> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class CommitmentType(val label: String) {
    EVENT("Event"),
    APPOINTMENT("Appointment"),
    SCHOOL("School"),
    BIRTHDAY("Birthday"),
    ANNIVERSARY("Anniversary"),
    OTHER("Other")
}
