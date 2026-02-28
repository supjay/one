package com.raven.app.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.Note
import com.raven.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val activeTag: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeTag = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NoteUiState> = combine(
        combine(_searchQuery, _activeTag) { query, tag -> Pair(query, tag) }
            .flatMapLatest { (query, tag) ->
                when {
                    query.isNotBlank() -> repository.searchNotes(query)
                    tag != null -> repository.getNotesByTag(tag)
                    else -> repository.getAllNotes()
                }
            },
        _searchQuery,
        _activeTag,
        _error
    ) { notes, query, tag, error ->
        NoteUiState(notes = notes, searchQuery = query, activeTag = tag, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteUiState())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTag(tag: String?) {
        _activeTag.value = tag
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.saveNote(note)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.setPinned(note.id, !note.isPinned)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
