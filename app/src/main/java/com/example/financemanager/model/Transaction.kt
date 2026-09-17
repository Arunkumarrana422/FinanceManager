package com.example.financemanager.model

/**
 * A single ledger entry. For TRANSFER, fromAccountId/toAccountId are both set
 * and accountId/category are left blank; for EXPENSE/INCOME, accountId +
 * category are set and fromAccountId/toAccountId are blank.
 * amountPaise is always positive; direction comes from `type`.
 */
data class Transaction(
    val transactionId: String = "",
    val userId: String = "",
    val type: String = TransactionType.EXPENSE,
    val amountPaise: Long = 0L,
    val title: String = "",
    val category: String = "",
    val accountId: String = "",
    val accountName: String = "",
    val fromAccountId: String = "",
    val fromAccountName: String = "",
    val toAccountId: String = "",
    val toAccountName: String = "",
    val paymentMethod: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
