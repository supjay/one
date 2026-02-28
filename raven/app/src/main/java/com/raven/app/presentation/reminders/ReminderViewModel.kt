package com.raven.app.presentation.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.Priority
import com.raven.app.domain.model.Reminder
import com.raven.app.domain.model.RepeatMode
import com.raven.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _showCompleted = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReminderUiState> = combine(
        _showCompleted.flatMapLatest { showCompleted ->
            if (showCompleted) repository.getAllReminders()
            else repository.getActiveReminders()
        },
        _showCompleted,
        _error
    ) { reminders, showCompleted, error ->
        ReminderUiState(reminders = reminders, showCompleted = showCompleted, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReminderUiState())

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                repository.saveReminder(reminder)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(reminder)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun setCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setCompleted(id, completed)
        }
    }

    fun deleteCompleted() {
        viewModelScope.launch {
            repository.deleteCompletedReminders()
        }
    }

    fun clearError() {
        _error.value = null
    }
}
