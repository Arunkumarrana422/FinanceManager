package com.example.financemanager.utils

import com.example.financemanager.model.Budget
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType

data class PeriodSummary(
    val income: Long,
    val expense: Long,
    val net: Long // income - expense
)

data class BudgetUsage(
    val budget: Budget,
    val spentPaise: Long,
    val remainingPaise: Long,
    val percentUsed: Int // 0-100+ (can exceed 100 if over budget)
)

/**
 * All dashboard/report/budget math funnels through here — single source of
 * truth for totals so no two screens can disagree on a number.
 */
object FinanceCalculator {

    fun summaryFor(transactions: List<Transaction>): PeriodSummary {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
        return PeriodSummary(income = income, expense = expense, net = income - expense)
    }

    fun todaySummary(allTransactions: List<Transaction>): PeriodSummary =
        summaryFor(allTransactions.filter { DateUtils.isToday(it.date) })

    fun monthSummary(allTransactions: List<Transaction>, monthKey: String): PeriodSummary =
        summaryFor(allTransactions.filter { DateUtils.monthKey(it.date) == monthKey })

    fun categorySpend(transactions: List<Transaction>): Map<String, Long> =
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amountPaise } }

    fun budgetUsage(budgets: List<Budget>, monthTransactions: List<Transaction>): List<BudgetUsage> {
        val spendByCategory = categorySpend(monthTransactions)
        val totalExpense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
        return budgets.map { budget ->
            val spent = if (budget.category.isBlank()) totalExpense else spendByCategory[budget.category] ?: 0L
            val remaining = budget.limitPaise - spent
            val percent = if (budget.limitPaise <= 0) 0 else ((spent * 100) / budget.limitPaise).toInt()
            BudgetUsage(budget, spent, remaining, percent)
        }
    }

    fun highestSpendingCategory(monthTransactions: List<Transaction>): Pair<String, Long>? =
        categorySpend(monthTransactions).maxByOrNull { it.value }?.toPair()

    fun largestExpense(monthTransactions: List<Transaction>): Transaction? =
        monthTransactions.filter { it.type == TransactionType.EXPENSE }.maxByOrNull { it.amountPaise }
}
