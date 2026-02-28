package com.raven.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relation: String, // Spouse, Child, Parent, Sibling, etc.
    val dateOfBirthMillis: Long? = null,
    val avatarColor: String = "#6200EE",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commitments")
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long,
    val type: String = "EVENT", // EVENT, APPOINTMENT, SCHOOL, BIRTHDAY, ANNIVERSARY
    val isRecurring: Boolean = false,
    val recurringPattern: String = "", // YEARLY, MONTHLY, WEEKLY
    val location: String = "",
    val isCompleted: Boolean = false,
    val reminderMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commitment_members")
data class CommitmentMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commitmentId: Long,
    val memberId: Long
)
