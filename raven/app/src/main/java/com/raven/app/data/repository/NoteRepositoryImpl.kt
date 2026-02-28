package com.raven.app.data.repository

import com.raven.app.data.local.dao.NoteDao
import com.raven.app.data.local.entities.NoteEntity
import com.raven.app.domain.model.Note
import com.raven.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        dao.getAllNotes().map { list -> list.map { it.toDomain() } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        dao.searchNotes(query).map { list -> list.map { it.toDomain() } }

    override fun getNotesByTag(tag: String): Flow<List<Note>> =
        dao.getNotesByTag(tag).map { list -> list.map { it.toDomain() } }

    override fun getPinnedNotes(): Flow<List<Note>> =
        dao.getPinnedNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): Note? =
        dao.getNoteById(id)?.toDomain()

    override suspend fun saveNote(note: Note): Long =
        dao.insertNote(note.toEntity())

    override suspend fun deleteNote(note: Note) =
        dao.deleteNote(note.toEntity())

    override suspend fun setPinned(id: Long, pinned: Boolean) =
        dao.setNotePinned(id, pinned)

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        content = content,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        color = color,
        isPinned = isPinned,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Note.toEntity() = NoteEntity(
        id = id,
        title = title,
        content = content,
        tags = tags.joinToString(","),
        color = color,
        isPinned = isPinned,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
