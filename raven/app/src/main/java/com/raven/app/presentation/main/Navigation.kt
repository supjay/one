package com.raven.app.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class RavenScreen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : RavenScreen("dashboard", "Home", Icons.Filled.Home)
    object Reminders : RavenScreen("reminders", "Reminders", Icons.Filled.Notifications)
    object Notes : RavenScreen("notes", "Notes", Icons.Filled.Description)
    object Expenses : RavenScreen("expenses", "Expenses", Icons.Filled.AccountBalance)
    object Travel : RavenScreen("travel", "Travel", Icons.Filled.FlightTakeoff)
    object Family : RavenScreen("family", "Family", Icons.Filled.FamilyRestroom)
    object Assistant : RavenScreen("assistant", "Raven", Icons.Filled.AutoAwesome)

    // Sub-routes
    object AddEditReminder : RavenScreen("reminders/add?id={id}", "Add Reminder", Icons.Filled.Add)
    object AddEditNote : RavenScreen("notes/add?id={id}", "Add Note", Icons.Filled.Add)
    object AddEditExpense : RavenScreen("expenses/add?id={id}", "Add Expense", Icons.Filled.Add)
    object TripDetail : RavenScreen("travel/{tripId}", "Trip Detail", Icons.Filled.Map)
    object AddEditTrip : RavenScreen("travel/add?id={id}", "Add Trip", Icons.Filled.Add)
    object AddEditCommitment : RavenScreen("family/commitment/add?id={id}", "Add Commitment", Icons.Filled.Add)
    object AddFamilyMember : RavenScreen("family/member/add?id={id}", "Add Member", Icons.Filled.PersonAdd)
}

val bottomNavItems = listOf(
    RavenScreen.Dashboard,
    RavenScreen.Reminders,
    RavenScreen.Notes,
    RavenScreen.Expenses,
    RavenScreen.Travel,
    RavenScreen.Family,
    RavenScreen.Assistant
)
