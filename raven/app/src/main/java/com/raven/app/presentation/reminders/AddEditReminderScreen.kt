package com.raven.app.presentation.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raven.app.domain.model.Priority
import com.raven.app.domain.model.Reminder
import com.raven.app.domain.model.RepeatMode
import com.raven.app.presentation.theme.ColorReminders
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    reminderId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val isEditing = reminderId > 0
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedRepeat by remember { mutableStateOf(RepeatMode.NONE) }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 3600_000) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var activeVoiceField by remember { mutableStateOf<String?>(null) }

    // Load existing reminder for edit
    LaunchedEffect(reminderId) {
        if (isEditing) {
            val existing = viewModel.uiState.value.reminders.firstOrNull { it.id == reminderId }
            existing?.let {
                title = it.title
                description = it.description
                selectedPriority = it.priority
                selectedRepeat = it.repeatMode
                dateMillis = it.dateTimeMillis
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Reminder" else "New Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveReminder(
                                    Reminder(
                                        id = if (isEditing) reminderId else 0,
                                        title = title.trim(),
                                        description = description.trim(),
                                        dateTimeMillis = dateMillis,
                                        priority = selectedPriority,
                                        repeatMode = selectedRepeat
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save", color = ColorReminders)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { activeVoiceField = "title" }) {
                        Icon(Icons.Filled.Mic, "Voice input", tint = ColorReminders)
                    }
                },
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                trailingIcon = {
                    IconButton(onClick = { activeVoiceField = "description" }) {
                        Icon(Icons.Filled.Mic, "Voice input", tint = ColorReminders)
                    }
                }
            )

            // Priority selector
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.label) }
                    )
                }
            }

            // Repeat mode
            Text("Repeat", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedRepeat == mode,
                        onClick = { selectedRepeat = mode },
                        label = { Text(mode.label) }
                    )
                }
            }

            // Date/Time
            Text("Date & Time", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(android.text.format.DateFormat.format("MMM dd, yyyy", dateMillis).toString()) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text(android.text.format.DateFormat.format("hh:mm a", dateMillis).toString()) },
                    leadingIcon = { Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp)) }
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        val selCal = Calendar.getInstance().apply { timeInMillis = selected }
                        cal.set(Calendar.YEAR, selCal.get(Calendar.YEAR))
                        cal.set(Calendar.MONTH, selCal.get(Calendar.MONTH))
                        cal.set(Calendar.DAY_OF_MONTH, selCal.get(Calendar.DAY_OF_MONTH))
                        dateMillis = cal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val c = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    c.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    c.set(Calendar.MINUTE, timePickerState.minute)
                    dateMillis = c.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
