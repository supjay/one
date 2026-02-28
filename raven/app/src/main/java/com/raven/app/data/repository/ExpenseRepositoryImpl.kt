package com.raven.app.data.repository

import com.raven.app.data.local.dao.ExpenseDao
import com.raven.app.data.local.entities.BudgetEntity
import com.raven.app.data.local.entities.ExpenseCategoryEntity
import com.raven.app.data.local.entities.ExpenseEntity
import com.raven.app.domain.model.*
import com.raven.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    override fun getExpensesByDateRange(startMillis: Long, endMillis: Long): Flow<List<Expense>> =
        dao.getExpensesByDateRange(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> =
        dao.getExpensesByCategory(category).map { list -> list.map { it.toDomain() } }

    override fun getTotalExpenseForPeriod(startMillis: Long, endMillis: Long): Flow<Double> =
        dao.getTotalExpenseForPeriod(startMillis, endMillis)

    override fun getTotalIncomeForPeriod(startMillis: Long, endMillis: Long): Flow<Double> =
        dao.getTotalIncomeForPeriod(startMillis, endMillis)

    override suspend fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)?.toDomain()

    override suspend fun saveExpense(expense: Expense): Long =
        dao.insertExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense.toEntity())

    override fun getAllCategories(): Flow<List<ExpenseCategory>> =
        dao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCategory(category: ExpenseCategory): Long =
        dao.insertCategory(category.toEntity())

    override suspend fun deleteCategory(category: ExpenseCategory) =
        dao.deleteCategory(category.toEntity())

    override fun getAllBudgets(): Flow<List<Budget>> =
        dao.getAllBudgets().map { list -> list.map { it.toDomain() } }

    override suspend fun saveBudget(budget: Budget): Long =
        dao.insertBudget(budget.toEntity())

    override suspend fun deleteBudget(budget: Budget) =
        dao.deleteBudget(budget.toEntity())

    override suspend fun getSpentAmountForBudget(category: String, startMillis: Long, endMillis: Long): Double =
        dao.getExpenseSumByCategory(category, startMillis, endMillis)

    private fun ExpenseEntity.toDomain() = Expense(
        id = id, title = title, amount = amount,
        type = ExpenseType.valueOf(type), category = category,
        note = note, dateMillis = dateMillis, createdAt = createdAt
    )

    private fun Expense.toEntity() = ExpenseEntity(
        id = id, title = title, amount = amount,
        type = type.name, category = category,
        note = note, dateMillis = dateMillis, createdAt = createdAt
    )

    private fun ExpenseCategoryEntity.toDomain() = ExpenseCategory(
        id = id, name = name, icon = icon, color = color, isDefault = isDefault
    )

    private fun ExpenseCategory.toEntity() = ExpenseCategoryEntity(
        id = id, name = name, icon = icon, color = color, isDefault = isDefault
    )

    private fun BudgetEntity.toDomain() = Budget(
        id = id, category = category, limitAmount = limitAmount,
        period = BudgetPeriod.valueOf(period), createdAt = createdAt
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id, category = category, limitAmount = limitAmount,
        period = period.name, createdAt = createdAt
    )
}
