package com.raven.app.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.raven.app.presentation.expenses.ExpenseViewModel
import com.raven.app.presentation.family.FamilyViewModel
import com.raven.app.presentation.reminders.ReminderViewModel
import com.raven.app.presentation.theme.*
import com.raven.app.presentation.travel.TravelViewModel
import com.raven.app.util.DateTimeUtils

@Composable
fun DashboardScreen(
    navController: NavController,
    reminderViewModel: ReminderViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    travelViewModel: TravelViewModel = hiltViewModel(),
    familyViewModel: FamilyViewModel = hiltViewModel()
) {
    val reminders by reminderViewModel.uiState.collectAsState()
    val expenses by expenseViewModel.uiState.collectAsState()
    val travel by travelViewModel.uiState.collectAsState()
    val family by familyViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Good ${greeting()}, Raven",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = DateTimeUtils.formatDate(System.currentTimeMillis()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick actions row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { QuickActionChip("Reminder", Icons.Filled.Notifications, ColorReminders) { navController.navigate("reminders/add?id=-1") } }
                item { QuickActionChip("Note", Icons.Filled.Description, ColorNotes) { navController.navigate("notes/add?id=-1") } }
                item { QuickActionChip("Expense", Icons.Filled.AccountBalance, ColorExpenses) { navController.navigate("expenses/add?id=-1") } }
                item { QuickActionChip("Ask Raven", Icons.Filled.AutoAwesome, ColorAssistant) { navController.navigate(RavenScreen.Assistant.route) } }
            }
        }

        // Upcoming reminders
        if (reminders.reminders.isNotEmpty()) {
            item {
                SectionHeader("Upcoming Reminders", ColorReminders) { navController.navigate(RavenScreen.Reminders.route) }
            }
            items(reminders.reminders.take(3)) { reminder ->
                DashboardCard(
                    title = reminder.title,
                    subtitle = DateTimeUtils.formatRelative(reminder.dateTimeMillis),
                    accentColor = ColorReminders,
                    icon = Icons.Filled.Notifications,
                    onClick = { navController.navigate(RavenScreen.Reminders.route) }
                )
            }
        }

        // Finance snapshot
        item {
            SectionHeader("This Month", ColorExpenses) { navController.navigate(RavenScreen.Expenses.route) }
            FinanceSummaryCard(
                income = expenses.summary.totalIncome,
                expense = expenses.summary.totalExpense,
                balance = expenses.summary.balance,
                onClick = { navController.navigate(RavenScreen.Expenses.route) }
            )
        }

        // Upcoming trips
        if (travel.trips.isNotEmpty()) {
            item {
                SectionHeader("Travel", ColorTravel) { navController.navigate(RavenScreen.Travel.route) }
            }
            items(travel.trips.filter { it.endDateMillis > System.currentTimeMillis() }.take(2)) { trip ->
                DashboardCard(
                    title = trip.name,
                    subtitle = "${trip.destination} · ${DateTimeUtils.formatShortDate(trip.startDateMillis)}",
                    accentColor = ColorTravel,
                    icon = Icons.Filled.FlightTakeoff,
                    onClick = { navController.navigate("travel/${trip.id}") }
                )
            }
        }

        // Upcoming family commitments
        if (family.commitments.isNotEmpty()) {
            item {
                SectionHeader("Family", ColorFamily) { navController.navigate(RavenScreen.Family.route) }
            }
            items(family.commitments.take(3)) { commitment ->
                DashboardCard(
                    title = commitment.title,
                    subtitle = "${commitment.type.label} · ${DateTimeUtils.formatRelative(commitment.dateTimeMillis)}",
                    accentColor = ColorFamily,
                    icon = Icons.Filled.FamilyRestroom,
                    onClick = { navController.navigate(RavenScreen.Family.route) }
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(4.dp, 20.dp).clip(CircleShape).background(color))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSeeAll) {
            Text("See all", style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accentColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FinanceSummaryCard(income: Double, expense: Double, balance: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorExpenses.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FinanceItem("Income", income, ColorExpenses)
            FinanceItem("Expenses", expense, MaterialTheme.colorScheme.error)
            FinanceItem("Balance", balance, if (balance >= 0) ColorExpenses else MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun FinanceItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$${"%.0f".format(amount)}", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}
