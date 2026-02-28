package com.raven.app.di

import android.content.Context
import androidx.room.Room
import com.raven.app.data.local.RavenDatabase
import com.raven.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RavenDatabase =
        Room.databaseBuilder(context, RavenDatabase::class.java, RavenDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideReminderDao(db: RavenDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideNoteDao(db: RavenDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideExpenseDao(db: RavenDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideTravelDao(db: RavenDatabase): TravelDao = db.travelDao()

    @Provides
    fun provideFamilyDao(db: RavenDatabase): FamilyDao = db.familyDao()

    @Provides
    fun provideAssistantDao(db: RavenDatabase): AssistantDao = db.assistantDao()
}
