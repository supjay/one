package com.raven.app.presentation.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raven.app.domain.model.Commitment
import com.raven.app.domain.model.CommitmentType
import com.raven.app.presentation.theme.ColorFamily
import com.raven.app.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCommitmentScreen(
    commitmentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: FamilyViewModel = hiltViewModel()
) {
    val isEditing = commitmentId > 0
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CommitmentType.EVENT) }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 86_400_000) }
    var isRecurring by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(commitmentId) {
        if (isEditing) {
            uiState.commitments.firstOrNull { it.id == commitmentId }?.let { c ->
                title = c.title
                description = c.description
                location = c.location
                selectedType = c.type
                dateMillis = c.dateTimeMillis
                isRecurring = c.isRecurring
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Commitment" else "New Commitment") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveCommitment(
                                    Commitment(
                                        id = if (isEditing) commitmentId else 0,
                                        title = title.trim(),
                                        description = description.trim(),
                                        location = location.trim(),
                                        type = selectedType,
                                        dateTimeMillis = dateMillis,
                                        isRecurring = isRecurring
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) { Text("Save", color = ColorFamily) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Filled.Mic, null, tint = ColorFamily) }, singleLine = true
            )

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth().height(80.dp)
            )

            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("Location (optional)") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) }, singleLine = true
            )

            // Type
            Text("Type", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CommitmentType.entries.take(3).forEach { type ->
                    FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type.label) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CommitmentType.entries.drop(3).forEach { type ->
                    FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type.label) })
                }
            }

            // Date/Time
            Text("Date & Time", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(DateTimeUtils.formatShortDate(dateMillis)) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text(android.text.format.DateFormat.format("hh:mm a", dateMillis).toString()) },
                    leadingIcon = { Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp)) }
                )
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Recurring", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { dateState.selectedDateMillis?.let { dateMillis = it }; showDatePicker = false }) { Text("OK") } }
        ) { DatePicker(state = dateState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val timeState = rememberTimePickerState(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val c = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    c.set(Calendar.HOUR_OF_DAY, timeState.hour)
                    c.set(Calendar.MINUTE, timeState.minute)
                    dateMillis = c.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            title = { Text("Select Time") },
            text = { TimePicker(state = timeState) }
        )
    }
}
