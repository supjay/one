package com.raven.app.presentation.expenses

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
import com.raven.app.domain.model.Expense
import com.raven.app.domain.model.ExpenseType
import com.raven.app.presentation.theme.ColorExpenses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    expenseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val isEditing = expenseId > 0
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ExpenseType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val defaultCategories = listOf("Food", "Transport", "Groceries", "Entertainment", "Health", "Shopping", "Bills", "Other")
    val categories = if (uiState.categories.isNotEmpty())
        uiState.categories.map { it.name }
    else defaultCategories

    LaunchedEffect(expenseId) {
        if (isEditing) {
            uiState.expenses.firstOrNull { it.id == expenseId }?.let { expense ->
                title = expense.title
                amount = expense.amount.toString()
                selectedType = expense.type
                selectedCategory = expense.category
                note = expense.note
            }
        }
        if (selectedCategory.isBlank() && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: return@TextButton
                            if (title.isNotBlank()) {
                                viewModel.saveExpense(
                                    Expense(
                                        id = if (isEditing) expenseId else 0,
                                        title = title.trim(),
                                        amount = amt,
                                        type = selectedType,
                                        category = selectedCategory,
                                        note = note.trim()
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank() && amount.toDoubleOrNull() != null
                    ) {
                        Text("Save", color = ColorExpenses)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Income / Expense toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label) },
                        leadingIcon = {
                            Icon(
                                if (type == ExpenseType.INCOME) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                null, modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Filled.Mic, "Voice", tint = ColorExpenses) },
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                singleLine = true
            )

            // Category
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
