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
import com.raven.app.domain.model.ItineraryItem
import com.raven.app.domain.model.ItineraryType
import com.raven.app.domain.model.PackingItem
import com.raven.app.presentation.theme.ColorTravel
import com.raven.app.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TravelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trip = uiState.trips.firstOrNull { it.id == tripId }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Itinerary", "Packing")

    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.name ?: "Trip Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    trip?.let {
                        IconButton(onClick = { /* edit */ }) {
                            Icon(Icons.Filled.Edit, "Edit trip")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            trip?.let { t ->
                // Trip header
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorTravel.copy(0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.LocationOn, null, tint = ColorTravel)
                            Text(t.destination, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("${DateTimeUtils.formatDate(t.startDateMillis)} → ${DateTimeUtils.formatDate(t.endDateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (t.budget > 0) Text("Budget: $${"%.0f".format(t.budget)}", style = MaterialTheme.typography.bodySmall, color = ColorTravel)
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> ItineraryTab(uiState.itinerary, tripId, viewModel)
                1 -> PackingTab(uiState.packingList, tripId, viewModel)
            }
        }
    }
}

@Composable
private fun ItineraryTab(items: List<ItineraryItem>, tripId: Long, viewModel: TravelViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No itinerary items yet. Tap + to add.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItineraryCard(item, onDelete = { viewModel.deleteItineraryItem(item) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = ColorTravel
        ) { Icon(Icons.Filled.Add, "Add item") }
    }

    if (showAddDialog) {
        AddItineraryDialog(
            tripId = tripId,
            onDismiss = { showAddDialog = false },
            onSave = { viewModel.saveItineraryItem(it); showAddDialog = false }
        )
    }
}

@Composable
private fun ItineraryCard(item: ItineraryItem, onDelete: () -> Unit) {
    val typeIcon = when (item.type) {
        ItineraryType.FLIGHT -> Icons.Filled.FlightTakeoff
        ItineraryType.HOTEL -> Icons.Filled.Hotel
        ItineraryType.ACTIVITY -> Icons.Filled.Attractions
        ItineraryType.TRANSPORT -> Icons.Filled.DirectionsCar
        ItineraryType.MEAL -> Icons.Filled.Restaurant
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(typeIcon, null, tint = ColorTravel, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(DateTimeUtils.formatDateTime(item.dateTimeMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.location.isNotBlank()) Text(item.location, style = MaterialTheme.typography.bodySmall, color = ColorTravel)
                if (item.isBooked) Text("Booked ✓", style = MaterialTheme.typography.labelSmall, color = ColorTravel)
            }
            if (item.estimatedCost > 0) Text("$${"%.0f".format(item.estimatedCost)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
private fun PackingTab(items: List<PackingItem>, tripId: Long, viewModel: TravelViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    val packed = items.count { it.isPacked }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                if (items.isNotEmpty()) {
                    Text("$packed / ${items.size} packed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (items.isNotEmpty()) packed.toFloat() / items.size else 0f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = ColorTravel
                    )
                }
            }
            items(items, key = { it.id }) { item ->
                PackingCard(item, onToggle = { viewModel.togglePackingItem(item) }, onDelete = { viewModel.deletePackingItem(item) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
        FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = ColorTravel) {
            Icon(Icons.Filled.Add, null)
        }
    }

    if (showAddDialog) {
        AddPackingItemDialog(tripId = tripId, onDismiss = { showAddDialog = false }, onSave = { viewModel.savePackingItem(it); showAddDialog = false })
    }
}

@Composable
private fun PackingCard(item: PackingItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(checked = item.isPacked, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = ColorTravel))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                Text("${item.category} · Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
private fun AddItineraryDialog(tripId: Long, onDismiss: () -> Unit, onSave: (ItineraryItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ItineraryType.ACTIVITY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Itinerary Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ItineraryType.entries.forEach { type ->
                        FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(ItineraryItem(tripId = tripId, title = title.trim(), location = location.trim(), dateTimeMillis = System.currentTimeMillis(), type = selectedType))
                }
            }, enabled = title.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddPackingItemDialog(tripId: Long, onDismiss: () -> Unit, onSave: (PackingItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var quantity by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Packing Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Qty:", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Filled.Remove, null) }
                    Text("$quantity", style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { quantity++ }) { Icon(Icons.Filled.Add, null) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(PackingItem(tripId = tripId, name = name.trim(), category = category.trim(), quantity = quantity))
            }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
