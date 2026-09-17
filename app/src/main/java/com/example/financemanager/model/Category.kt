package com.example.financemanager.model

object TransactionType {
    const val EXPENSE = "EXPENSE"
    const val INCOME = "INCOME"
    const val TRANSFER = "TRANSFER"
}

object DefaultCategories {
    val EXPENSE = listOf(
        "Food", "Grocery", "Rent", "Electricity", "Water", "Internet", "Mobile",
        "Fuel", "Transport", "Shopping", "Education", "Health", "Entertainment",
        "Insurance", "EMI", "Travel", "Personal", "Other"
    )
    val INCOME = listOf(
        "Salary", "Business", "Freelancing", "Agriculture", "Bonus", "Interest", "Gift", "Other"
    )
}

data class Category(
    val categoryId: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = TransactionType.EXPENSE, // EXPENSE or INCOME
    val icon: String = "Category", // maps to a Material icon name in the UI layer
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
