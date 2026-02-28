package com.raven.app.presentation.expenses

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
import com.raven.app.domain.model.Expense
import com.raven.app.domain.model.ExpenseType
import com.raven.app.presentation.reminders.EmptyState
import com.raven.app.presentation.theme.ColorExpenses
import com.raven.app.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                actions = {
                    Text(
                        DateTimeUtils.formatMonthYear(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("expenses/add?id=-1") },
                containerColor = ColorExpenses
            ) {
                Icon(Icons.Filled.Add, "Add expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary card
            item {
                ExpenseSummaryCard(uiState.summary)
            }

            // Budget progress
            if (uiState.budgets.isNotEmpty()) {
                item { Text("Budgets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(uiState.budgets) { budget ->
                    BudgetProgressCard(budget)
                }
            }

            // Transactions
            if (uiState.expenses.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.AccountBalance,
                        message = "No transactions yet",
                        subtitle = "Tap + to log income or expenses"
                    )
                }
            } else {
                item { Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(uiState.expenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onEdit = { navController.navigate("expenses/add?id=${expense.id}") },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ExpenseSummaryCard(summary: ExpenseSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorExpenses.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem("Income", summary.totalIncome, ColorExpenses)
            SummaryItem("Spent", summary.totalExpense, MaterialTheme.colorScheme.error)
            SummaryItem("Balance", summary.balance, if (summary.balance >= 0) ColorExpenses else MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$${"%.2f".format(amount)}", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BudgetProgressCard(budget: com.raven.app.domain.model.Budget) {
    val fraction = if (budget.limitAmount > 0) (budget.spentAmount / budget.limitAmount).toFloat().coerceIn(0f, 1f) else 0f
    val color = when {
        fraction < 0.7f -> ColorExpenses
        fraction < 0.9f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(budget.category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("$${"%.0f".format(budget.spentAmount)} / $${"%.0f".format(budget.limitAmount)}", style = MaterialTheme.typography.bodySmall, color = color)
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val isIncome = expense.type == ExpenseType.INCOME
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = (if (isIncome) ColorExpenses else MaterialTheme.colorScheme.error).copy(0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        null, tint = if (isIncome) ColorExpenses else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${expense.category} · ${DateTimeUtils.formatShortDate(expense.dateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${if (isIncome) "+" else "-"}$${"%.2f".format(expense.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) ColorExpenses else MaterialTheme.colorScheme.error
            )
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
