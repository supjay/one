package com.raven.app.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raven.app.domain.model.Note
import com.raven.app.presentation.theme.ColorNotes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val isEditing = noteId > 0
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(noteId) {
        if (isEditing) {
            viewModel.getAllNotes().firstOrNull { it.id == noteId }?.let { note ->
                title = note.title
                content = note.content
                tagsInput = note.tags.joinToString(", ")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditing) {
            try { contentFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Note" else "New Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                viewModel.saveNote(
                                    Note(
                                        id = if (isEditing) noteId else 0,
                                        title = title.trim(),
                                        content = content.trim(),
                                        tags = tags
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank() || content.isNotBlank()
                    ) {
                        Text("Save", color = ColorNotes)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Filled.Mic, "Voice input", tint = ColorNotes)
                },
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp).focusRequester(contentFocusRequester),
                trailingIcon = {
                    Icon(Icons.Filled.Mic, "Voice input", tint = ColorNotes)
                }
            )

            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. work, ideas, personal") },
                leadingIcon = { Icon(Icons.Filled.Tag, null) },
                singleLine = true
            )
        }
    }
}

// Extension helper — in a real app this would use the repo directly
private fun NoteViewModel.getAllNotes(): List<com.raven.app.domain.model.Note> =
    uiState.value.notes
