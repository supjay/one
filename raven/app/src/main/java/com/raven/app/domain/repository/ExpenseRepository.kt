package com.raven.app.domain.repository

import com.raven.app.domain.model.Budget
import com.raven.app.domain.model.Expense
import com.raven.app.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByDateRange(startMillis: Long, endMillis: Long): Flow<List<Expense>>
    fun getExpensesByCategory(category: String): Flow<List<Expense>>
    fun getTotalExpenseForPeriod(startMillis: Long, endMillis: Long): Flow<Double>
    fun getTotalIncomeForPeriod(startMillis: Long, endMillis: Long): Flow<Double>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun saveExpense(expense: Expense): Long
    suspend fun deleteExpense(expense: Expense)

    fun getAllCategories(): Flow<List<ExpenseCategory>>
    suspend fun saveCategory(category: ExpenseCategory): Long
    suspend fun deleteCategory(category: ExpenseCategory)

    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun saveBudget(budget: Budget): Long
    suspend fun deleteBudget(budget: Budget)
    suspend fun getSpentAmountForBudget(category: String, startMillis: Long, endMillis: Long): Double
}
