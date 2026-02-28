package com.raven.app.domain.model

data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: ExpenseType = ExpenseType.EXPENSE,
    val category: String,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

data class ExpenseCategory(
    val id: Long = 0,
    val name: String,
    val icon: String = "category",
    val color: String = "#6200EE",
    val isDefault: Boolean = false
)

data class Budget(
    val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val spentAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ExpenseType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income")
}

enum class BudgetPeriod(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
