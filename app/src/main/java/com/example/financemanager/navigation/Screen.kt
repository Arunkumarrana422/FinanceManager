package com.example.financemanager.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")

    data object Dashboard : Screen("dashboard")
    data object AddExpense : Screen("add_expense?transactionId={transactionId}") {
        fun build(transactionId: String? = null) = "add_expense?transactionId=${transactionId ?: ""}"
    }
    data object AddIncome : Screen("add_income?transactionId={transactionId}") {
        fun build(transactionId: String? = null) = "add_income?transactionId=${transactionId ?: ""}"
    }
    data object AddTransfer : Screen("add_transfer")
    data object Transactions : Screen("transactions")
    data object Accounts : Screen("accounts")
    data object Budgets : Screen("budgets")
    data object Profile : Screen("profile")
}
