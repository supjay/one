package com.raven.app.presentation.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.raven.app.domain.model.Commitment
import com.raven.app.domain.model.CommitmentType
import com.raven.app.domain.model.FamilyMember
import com.raven.app.presentation.reminders.EmptyState
import com.raven.app.presentation.theme.ColorFamily
import com.raven.app.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    navController: NavController,
    viewModel: FamilyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Commitments", "Members")
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family") },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                            Icon(if (uiState.showCompleted) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) navController.navigate("family/commitment/add?id=-1")
                    else showAddMemberDialog = true
                },
                containerColor = ColorFamily
            ) { Icon(Icons.Filled.Add, "Add") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> CommitmentsTab(uiState.commitments, navController, viewModel)
                1 -> MembersTab(uiState.members, viewModel)
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { viewModel.saveMember(it); showAddMemberDialog = false }
        )
    }
}

@Composable
private fun CommitmentsTab(commitments: List<Commitment>, navController: NavController, viewModel: FamilyViewModel) {
    // Type filter chips
    Column {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = true, onClick = { viewModel.setTypeFilter(null) }, label = { Text("All") })
            CommitmentType.entries.take(4).forEach { type ->
                FilterChip(selected = false, onClick = { viewModel.setTypeFilter(type) }, label = { Text(type.label) })
            }
        }

        if (commitments.isEmpty()) {
            EmptyState(icon = Icons.Filled.FamilyRestroom, message = "No commitments", subtitle = "Tap + to add a family commitment")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commitments, key = { it.id }) { commitment ->
                    CommitmentCard(
                        commitment = commitment,
                        onToggle = { viewModel.setCommitmentCompleted(commitment.id, !commitment.isCompleted) },
                        onEdit = { navController.navigate("family/commitment/add?id=${commitment.id}") },
                        onDelete = { viewModel.deleteCommitment(commitment) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CommitmentCard(commitment: Commitment, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val typeColor = when (commitment.type) {
        CommitmentType.BIRTHDAY, CommitmentType.ANNIVERSARY -> MaterialTheme.colorScheme.tertiary
        CommitmentType.APPOINTMENT -> MaterialTheme.colorScheme.error
        CommitmentType.SCHOOL -> ColorFamily
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = commitment.isCompleted, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = ColorFamily))
            Column(modifier = Modifier.weight(1f)) {
                Text(commitment.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textDecoration = if (commitment.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(onClick = {}, label = { Text(commitment.type.label, style = MaterialTheme.typography.labelSmall) }, colors = SuggestionChipDefaults.suggestionChipColors(labelColor = typeColor))
                    Text(DateTimeUtils.formatRelative(commitment.dateTimeMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (commitment.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(commitment.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
    }
}

@Composable
private fun MembersTab(members: List<FamilyMember>, viewModel: FamilyViewModel) {
    if (members.isEmpty()) {
        EmptyState(icon = Icons.Filled.Group, message = "No family members", subtitle = "Tap + to add family members")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(members, key = { it.id }) { member ->
                MemberCard(member = member, onDelete = { viewModel.deleteMember(member) })
            }
        }
    }
}

@Composable
private fun MemberCard(member: FamilyMember, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val color = try { Color(android.graphics.Color.parseColor(member.avatarColor)) } catch (_: Exception) { ColorFamily }
            Box(modifier = Modifier.size(44.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.fillMaxSize(), color = color.copy(0.2f)) {}
                Text(member.name.first().uppercase(), style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(member.relation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                member.dateOfBirthMillis?.let {
                    Text("Born ${DateTimeUtils.formatShortDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null) }
        }
    }
}

@Composable
private fun AddMemberDialog(onDismiss: () -> Unit, onSave: (FamilyMember) -> Unit) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    val relations = listOf("Spouse", "Child", "Parent", "Sibling", "Other")
    var selectedRelation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    relations.forEach { rel ->
                        FilterChip(selected = selectedRelation == rel, onClick = { selectedRelation = rel; relation = rel }, label = { Text(rel, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(FamilyMember(name = name.trim(), relation = relation.ifBlank { "Other" })) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
