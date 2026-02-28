package com.raven.app.presentation.travel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raven.app.domain.model.Trip
import com.raven.app.domain.model.TripStatus
import com.raven.app.presentation.theme.ColorTravel
import com.raven.app.util.DateTimeUtils
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTripScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TravelViewModel = hiltViewModel()
) {
    val isEditing = tripId > 0
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endMillis by remember { mutableLongStateOf(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)) }
    var budget by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(TripStatus.PLANNED) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        if (isEditing) {
            uiState.trips.firstOrNull { it.id == tripId }?.let { trip ->
                name = trip.name
                destination = trip.destination
                startMillis = trip.startDateMillis
                endMillis = trip.endDateMillis
                budget = if (trip.budget > 0) trip.budget.toString() else ""
                notes = trip.notes
                selectedStatus = trip.status
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Trip" else "New Trip") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank() && destination.isNotBlank()) {
                                viewModel.saveTrip(
                                    Trip(
                                        id = if (isEditing) tripId else 0,
                                        name = name.trim(),
                                        destination = destination.trim(),
                                        startDateMillis = startMillis,
                                        endDateMillis = endMillis,
                                        budget = budget.toDoubleOrNull() ?: 0.0,
                                        status = selectedStatus,
                                        notes = notes.trim()
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = name.isNotBlank() && destination.isNotBlank()
                    ) { Text("Save", color = ColorTravel) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Trip Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(
                value = destination, onValueChange = { destination = it },
                label = { Text("Destination") }, modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) }, singleLine = true
            )

            // Date range
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showStartDatePicker = true },
                    label = { Text("From: ${DateTimeUtils.formatShortDate(startMillis)}") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { showEndDatePicker = true },
                    label = { Text("To: ${DateTimeUtils.formatShortDate(endMillis)}") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = budget, onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Budget (optional)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) }, singleLine = true
            )

            // Status
            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TripStatus.entries.forEach { status ->
                    FilterChip(selected = selectedStatus == status, onClick = { selectedStatus = status }, label = { Text(status.label) })
                }
            }

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth().height(100.dp)
            )
        }
    }

    if (showStartDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { startMillis = it }; showStartDatePicker = false }) { Text("OK") } }
        ) { DatePicker(state = state) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { endMillis = it }; showEndDatePicker = false }) { Text("OK") } }
        ) { DatePicker(state = state) }
    }
}
