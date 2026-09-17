package com.example.financemanager.model

/**
 * One budget = one category, one month. monthKey is "yyyy-MM".
 * "Overall" budgets (not tied to a category) use category = "" (blank).
 */
data class Budget(
    val budgetId: String = "",
    val userId: String = "",
    val category: String = "", // blank = overall monthly budget
    val monthKey: String = "",
    val limitPaise: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
