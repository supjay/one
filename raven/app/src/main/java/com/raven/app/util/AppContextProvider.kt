package com.raven.app.util

import com.raven.app.domain.repository.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregates data from all repositories into a structured context string.
 * Phase 2: This string is prepended to LLM prompts so Raven can answer
 * questions about the user's data (e.g. "What's my budget left this month?").
 */
@Singleton
class AppContextProvider @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val expenseRepository: ExpenseRepository,
    private val travelRepository: TravelRepository,
    private val familyRepository: FamilyRepository
) {

    private val dateFormat = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    suspend fun buildContext(): String {
        val sb = StringBuilder()
        sb.appendLine("=== RAVEN CONTEXT (Today: ${dateFormat.format(Date())}) ===")

        // Upcoming reminders
        try {
            val reminders = reminderRepository.getActiveReminders().first().take(5)
            if (reminders.isNotEmpty()) {
                sb.appendLine("\nUPCOMING REMINDERS:")
                reminders.forEach { r ->
                    sb.appendLine("  - ${r.title} at ${dateFormat.format(Date(r.dateTimeMillis))} [${r.priority.label} priority]")
                }
            } else {
                sb.appendLine("\nNo upcoming reminders.")
            }
        } catch (_: Exception) {}

        // Current month expenses
        try {
            val cal = Calendar.getInstance()
            val monthStart = cal.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis
            val monthEnd = System.currentTimeMillis()

            val totalExpense = expenseRepository.getTotalExpenseForPeriod(monthStart, monthEnd).first()
            val totalIncome = expenseRepository.getTotalIncomeForPeriod(monthStart, monthEnd).first()
            sb.appendLine("\nFINANCES (This month):")
            sb.appendLine("  Income: $${"%.2f".format(totalIncome)}")
            sb.appendLine("  Expenses: $${"%.2f".format(totalExpense)}")
            sb.appendLine("  Balance: $${"%.2f".format(totalIncome - totalExpense)}")

            val budgets = expenseRepository.getAllBudgets().first().take(5)
            if (budgets.isNotEmpty()) {
                sb.appendLine("  Budgets:")
                budgets.forEach { b ->
                    val spent = expenseRepository.getSpentAmountForBudget(b.category, monthStart, monthEnd)
                    val pct = if (b.limitAmount > 0) (spent / b.limitAmount * 100).toInt() else 0
                    sb.appendLine("    ${b.category}: $${"%.2f".format(spent)} / $${"%.2f".format(b.limitAmount)} ($pct%)")
                }
            }
        } catch (_: Exception) {}

        // Upcoming trips
        try {
            val now = System.currentTimeMillis()
            val trips = travelRepository.getAllTrips().first()
                .filter { it.endDateMillis > now }
                .sortedBy { it.startDateMillis }
                .take(3)
            if (trips.isNotEmpty()) {
                sb.appendLine("\nUPCOMING TRIPS:")
                trips.forEach { t ->
                    val daysUntil = ((t.startDateMillis - now) / (1000 * 60 * 60 * 24)).toInt()
                    sb.appendLine("  - ${t.name} to ${t.destination}: ${shortDateFormat.format(Date(t.startDateMillis))} - ${shortDateFormat.format(Date(t.endDateMillis))} (${if (daysUntil > 0) "in $daysUntil days" else "ongoing"})")
                }
            }
        } catch (_: Exception) {}

        // Upcoming family commitments
        try {
            val commitments = familyRepository.getUpcomingCommitments().first().take(5)
            if (commitments.isNotEmpty()) {
                sb.appendLine("\nFAMILY COMMITMENTS:")
                commitments.forEach { c ->
                    sb.appendLine("  - ${c.title} [${c.type.label}] on ${dateFormat.format(Date(c.dateTimeMillis))}")
                }
            }
        } catch (_: Exception) {}

        sb.appendLine("\n=== END CONTEXT ===")
        return sb.toString()
    }
}
