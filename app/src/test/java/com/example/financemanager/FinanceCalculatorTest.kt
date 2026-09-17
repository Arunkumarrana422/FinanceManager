package com.example.financemanager

import com.example.financemanager.model.Budget
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.utils.FinanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceCalculatorTest {

    private fun tx(type: String, amountPaise: Long, category: String = "Food", date: Long = System.currentTimeMillis()) =
        Transaction(type = type, amountPaise = amountPaise, category = category, date = date)

    @Test
    fun `summary sums income and expense separately and nets them`() {
        val transactions = listOf(
            tx(TransactionType.INCOME, 100_00),
            tx(TransactionType.EXPENSE, 30_00),
            tx(TransactionType.EXPENSE, 20_00)
        )
        val summary = FinanceCalculator.summaryFor(transactions)
        assertEquals(100_00L, summary.income)
        assertEquals(50_00L, summary.expense)
        assertEquals(50_00L, summary.net)
    }

    @Test
    fun `category spend groups only expenses by category`() {
        val transactions = listOf(
            tx(TransactionType.EXPENSE, 30_00, category = "Food"),
            tx(TransactionType.EXPENSE, 20_00, category = "Food"),
            tx(TransactionType.EXPENSE, 10_00, category = "Transport"),
            tx(TransactionType.INCOME, 500_00, category = "")
        )
        val spend = FinanceCalculator.categorySpend(transactions)
        assertEquals(50_00L, spend["Food"])
        assertEquals(10_00L, spend["Transport"])
        assertEquals(2, spend.size) // income excluded
    }

    @Test
    fun `budget usage computes remaining and percent correctly`() {
        val budget = Budget(budgetId = "b1", category = "Food", monthKey = "2026-09", limitPaise = 500_00)
        val transactions = listOf(tx(TransactionType.EXPENSE, 320_00, category = "Food"))
        val usage = FinanceCalculator.budgetUsage(listOf(budget), transactions)
        assertEquals(1, usage.size)
        assertEquals(320_00L, usage[0].spentPaise)
        assertEquals(180_00L, usage[0].remainingPaise)
        assertEquals(64, usage[0].percentUsed)
    }

    @Test
    fun `over-budget produces negative remaining and over-100 percent`() {
        val budget = Budget(budgetId = "b1", category = "Food", monthKey = "2026-09", limitPaise = 100_00)
        val transactions = listOf(tx(TransactionType.EXPENSE, 150_00, category = "Food"))
        val usage = FinanceCalculator.budgetUsage(listOf(budget), transactions)
        assertEquals(-50_00L, usage[0].remainingPaise)
        assertEquals(150, usage[0].percentUsed)
    }
}
