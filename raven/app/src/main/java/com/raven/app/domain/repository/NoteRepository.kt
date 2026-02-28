package com.raven.app.domain.repository

import com.raven.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun getNotesByTag(tag: String): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(note: Note)
    suspend fun setPinned(id: Long, pinned: Boolean)
}
