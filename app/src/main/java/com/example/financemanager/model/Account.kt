package com.example.financemanager.model

object AccountType {
    const val CASH = "Cash"
    const val BANK = "Bank"
    const val UPI = "UPI"
    const val WALLET = "Wallet"
    const val CREDIT_CARD = "Credit Card"
    const val OTHER = "Other"

    val ALL = listOf(CASH, BANK, UPI, WALLET, CREDIT_CARD, OTHER)
}

/**
 * currentBalancePaise is maintained by the repository transactionally whenever
 * a transaction/transfer touching this account is added, edited or deleted —
 * never recomputed by summing the full history on every read.
 */
data class Account(
    val accountId: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = AccountType.CASH,
    val openingBalancePaise: Long = 0L,
    val currentBalancePaise: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
