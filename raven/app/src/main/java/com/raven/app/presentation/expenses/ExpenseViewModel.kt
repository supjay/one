package com.raven.app.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.*
import com.raven.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ExpenseSummary(
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0
)

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<ExpenseCategory> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val summary: ExpenseSummary = ExpenseSummary(),
    val selectedPeriodStart: Long = currentMonthStart(),
    val selectedPeriodEnd: Long = currentMonthEnd(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _periodStart = MutableStateFlow(currentMonthStart())
    private val _periodEnd = MutableStateFlow(currentMonthEnd())
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExpenseUiState> = combine(
        combine(_periodStart, _periodEnd) { start, end ->
            repository.getExpensesByDateRange(start, end)
        }.flatMapLatest { it },
        repository.getAllCategories(),
        repository.getAllBudgets(),
        combine(_periodStart, _periodEnd) { start, end ->
            combine(
                repository.getTotalExpenseForPeriod(start, end),
                repository.getTotalIncomeForPeriod(start, end)
            ) { exp, inc -> ExpenseSummary(exp, inc, inc - exp) }
        }.flatMapLatest { it },
        _periodStart,
        _periodEnd,
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val expenses = args[0] as List<Expense>
        val categories = args[1] as List<ExpenseCategory>
        val budgets = args[2] as List<Budget>
        val summary = args[3] as ExpenseSummary
        val periodStart = args[4] as Long
        val periodEnd = args[5] as Long
        val error = args[6] as String?
        ExpenseUiState(expenses, categories, budgets, summary, periodStart, periodEnd, false, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseUiState())

    fun setPeriod(startMillis: Long, endMillis: Long) {
        _periodStart.value = startMillis
        _periodEnd.value = endMillis
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.saveExpense(expense)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveCategory(category: ExpenseCategory) {
        viewModelScope.launch { repository.saveCategory(category) }
    }

    fun deleteCategory(category: ExpenseCategory) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch { repository.saveBudget(budget) }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { repository.deleteBudget(budget) }
    }

    fun clearError() { _error.value = null }
}

private fun currentMonthStart(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun currentMonthEnd(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
