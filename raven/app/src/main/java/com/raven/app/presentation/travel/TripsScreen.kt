package com.raven.app.presentation.travel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.raven.app.domain.model.Trip
import com.raven.app.domain.model.TripStatus
import com.raven.app.presentation.reminders.EmptyState
import com.raven.app.presentation.theme.ColorTravel
import com.raven.app.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    navController: NavController,
    viewModel: TravelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) { Icon(Icons.Filled.FilterList, "Filter") }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("All") }, onClick = { viewModel.setStatusFilter(null); expanded = false })
                            TripStatus.entries.forEach { status ->
                                DropdownMenuItem(text = { Text(status.label) }, onClick = { viewModel.setStatusFilter(status); expanded = false })
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("travel/add?id=-1") },
                containerColor = ColorTravel
            ) {
                Icon(Icons.Filled.Add, "Add trip")
            }
        }
    ) { padding ->
        if (uiState.trips.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.FlightTakeoff,
                message = "No trips yet",
                subtitle = "Tap + to plan your next adventure",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.trips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { navController.navigate("travel/${trip.id}") },
                        onEdit = { navController.navigate("travel/add?id=${trip.id}") },
                        onDelete = { viewModel.deleteTrip(trip) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun TripCard(trip: Trip, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val statusColor = when (trip.status) {
        TripStatus.PLANNED -> ColorTravel
        TripStatus.ONGOING -> MaterialTheme.colorScheme.tertiary
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
        TripStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(trip.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(14.dp), tint = ColorTravel)
                        Text(trip.destination, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Filled.MoreVert, null) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Filled.Edit, null) })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Filled.Delete, null) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DateRange, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${DateTimeUtils.formatShortDate(trip.startDateMillis)} – ${DateTimeUtils.formatShortDate(trip.endDateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                SuggestionChip(
                    onClick = {},
                    label = { Text(trip.status.label, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(labelColor = statusColor)
                )
            }
            if (trip.budget > 0) {
                Text("Budget: $${"%.0f".format(trip.budget)}", style = MaterialTheme.typography.bodySmall, color = ColorTravel)
            }
        }
    }
}
