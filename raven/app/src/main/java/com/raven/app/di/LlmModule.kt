package com.raven.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.raven.app.util.LlmInferenceManager
import com.raven.app.util.ModelDownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "raven_prefs")

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {

    @Provides
    @Singleton
    fun provideLlmInferenceManager(
        @ApplicationContext context: Context
    ): LlmInferenceManager = LlmInferenceManager(context)

    @Provides
    @Singleton
    fun provideModelDownloadManager(
        @ApplicationContext context: Context
    ): ModelDownloadManager = ModelDownloadManager(context)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
