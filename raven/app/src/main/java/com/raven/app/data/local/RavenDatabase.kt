package com.raven.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raven.app.data.local.dao.*
import com.raven.app.data.local.entities.*

@Database(
    entities = [
        ReminderEntity::class,
        NoteEntity::class,
        ExpenseEntity::class,
        ExpenseCategoryEntity::class,
        BudgetEntity::class,
        TripEntity::class,
        ItineraryItemEntity::class,
        PackingItemEntity::class,
        FamilyMemberEntity::class,
        CommitmentEntity::class,
        CommitmentMemberEntity::class,
        ConversationMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RavenDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun travelDao(): TravelDao
    abstract fun familyDao(): FamilyDao
    abstract fun assistantDao(): AssistantDao

    companion object {
        const val DATABASE_NAME = "raven_db"
    }
}
