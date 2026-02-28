package com.raven.app.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.raven.app.presentation.assistant.AssistantScreen
import com.raven.app.presentation.expenses.AddEditExpenseScreen
import com.raven.app.presentation.expenses.ExpensesScreen
import com.raven.app.presentation.family.AddEditCommitmentScreen
import com.raven.app.presentation.family.FamilyScreen
import com.raven.app.presentation.notes.AddEditNoteScreen
import com.raven.app.presentation.notes.NotesScreen
import com.raven.app.presentation.reminders.AddEditReminderScreen
import com.raven.app.presentation.reminders.RemindersScreen
import com.raven.app.presentation.setup.ModelSetupScreen
import com.raven.app.presentation.setup.ModelSetupViewModel
import com.raven.app.presentation.travel.AddEditTripScreen
import com.raven.app.presentation.travel.TripDetailScreen
import com.raven.app.presentation.travel.TripsScreen

@Composable
fun RavenNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Check if first-run AI setup is needed
    val setupViewModel: ModelSetupViewModel = hiltViewModel()
    val isSetupNeeded by setupViewModel.isSetupNeeded.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(isSetupNeeded) {
        if (isSetupNeeded) {
            navController.navigate(RavenScreen.ModelSetup.route) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = RavenScreen.Dashboard.route,
        modifier = modifier
    ) {
        // AI Model Setup (first-run)
        composable(RavenScreen.ModelSetup.route) {
            ModelSetupScreen(
                onSetupComplete = {
                    navController.navigate(RavenScreen.Dashboard.route) {
                        popUpTo(RavenScreen.ModelSetup.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(RavenScreen.Dashboard.route) {
                        popUpTo(RavenScreen.ModelSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RavenScreen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        // Reminders
        composable(RavenScreen.Reminders.route) {
            RemindersScreen(navController = navController)
        }
        composable(
            route = "reminders/add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            AddEditReminderScreen(
                reminderId = backStackEntry.arguments?.getLong("id") ?: -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Notes
        composable(RavenScreen.Notes.route) {
            NotesScreen(navController = navController)
        }
        composable(
            route = "notes/add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            AddEditNoteScreen(
                noteId = backStackEntry.arguments?.getLong("id") ?: -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Expenses
        composable(RavenScreen.Expenses.route) {
            ExpensesScreen(navController = navController)
        }
        composable(
            route = "expenses/add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            AddEditExpenseScreen(
                expenseId = backStackEntry.arguments?.getLong("id") ?: -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Travel
        composable(RavenScreen.Travel.route) {
            TripsScreen(navController = navController)
        }
        composable(
            route = "travel/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            TripDetailScreen(
                tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "travel/add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            AddEditTripScreen(
                tripId = backStackEntry.arguments?.getLong("id") ?: -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Family
        composable(RavenScreen.Family.route) {
            FamilyScreen(navController = navController)
        }
        composable(
            route = "family/commitment/add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            AddEditCommitmentScreen(
                commitmentId = backStackEntry.arguments?.getLong("id") ?: -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Assistant
        composable(RavenScreen.Assistant.route) {
            AssistantScreen()
        }
    }
}
