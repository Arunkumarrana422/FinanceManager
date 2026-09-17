package com.example.financemanager.navigation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.ui.screens.account.AccountsScreen
import com.example.financemanager.ui.screens.auth.ForgotPasswordScreen
import com.example.financemanager.ui.screens.auth.LoginScreen
import com.example.financemanager.ui.screens.auth.SignUpScreen
import com.example.financemanager.ui.screens.budget.BudgetsScreen
import com.example.financemanager.ui.screens.dashboard.DashboardScreen
import com.example.financemanager.ui.screens.profile.ProfileScreen
import com.example.financemanager.ui.screens.splash.SplashScreen
import com.example.financemanager.ui.screens.transaction.AddTransactionScreen
import com.example.financemanager.ui.screens.transaction.AddTransferScreen
import com.example.financemanager.ui.screens.transaction.TransactionsScreen
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.utils.FinanceCalculator
import com.example.financemanager.viewmodel.AccountViewModel
import com.example.financemanager.viewmodel.AuthViewModel
import com.example.financemanager.viewmodel.BudgetViewModel
import com.example.financemanager.viewmodel.CategoryViewModel
import com.example.financemanager.viewmodel.TransactionViewModel

@Composable
fun FinanceManagerNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val accountViewModel: AccountViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val budgetViewModel: BudgetViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                isLoggedIn = authViewModel.isLoggedIn(),
                onNavigateNext = { loggedIn ->
                    if (loggedIn) {
                        authViewModel.loadCurrentUserProfile()
                    }
                    navController.navigate(if (loggedIn) Screen.Dashboard.route else Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    authViewModel.loadCurrentUserProfile()
                    navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = {
                    authViewModel.loadCurrentUserProfile()
                    navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(viewModel = authViewModel, onBackToLogin = { navController.popBackStack() })
        }

        composable(Screen.Dashboard.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val allTransactions by transactionViewModel.allTransactions.collectAsState()
            val accounts by accountViewModel.accounts.collectAsState()
            val budgetUsage by budgetViewModel.usage.collectAsState()

            remember(currentUser?.uid) {
                currentUser?.uid?.let { uid ->
                    transactionViewModel.observeTransactions(uid)
                    accountViewModel.observeAccounts(uid)
                    categoryViewModel.observeCategories(uid)
                    budgetViewModel.start(uid, transactionViewModel.allTransactions)
                }
                true
            }

            val monthKey = DateUtils.currentMonthKey()
            val todaySummary = FinanceCalculator.todaySummary(allTransactions)
            val monthSummary = FinanceCalculator.monthSummary(allTransactions, monthKey)
            val overallBudget = budgetUsage.find { it.budget.category.isBlank() }

            DashboardScreen(
                todaySummary = todaySummary,
                monthSummary = monthSummary,
                remainingBudget = overallBudget?.remainingPaise,
                recentTransactions = allTransactions,
                onAddExpense = { navController.navigate(Screen.AddExpense.build()) },
                onAddIncome = { navController.navigate(Screen.AddIncome.build()) },
                onAddTransfer = { navController.navigate(Screen.AddTransfer.route) },
                onOpenTransactions = { navController.navigate(Screen.Transactions.route) },
                onOpenAccounts = { navController.navigate(Screen.Accounts.route) },
                onOpenBudgets = { navController.navigate(Screen.Budgets.route) },
                onOpenProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(
            Screen.AddExpense.route,
            arguments = listOf(navArgument("transactionId") { nullable = true; defaultValue = "" })
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("transactionId")
            val currentUser by authViewModel.currentUser.collectAsState()
            val accounts by accountViewModel.accounts.collectAsState()
            val categories by categoryViewModel.categories.collectAsState()
            val allTransactions by transactionViewModel.allTransactions.collectAsState()
            val existing = allTransactions.find { it.transactionId == txId }

            currentUser?.let { user ->
                AddTransactionScreen(
                    viewModel = transactionViewModel,
                    uid = user.uid,
                    type = TransactionType.EXPENSE,
                    accounts = accounts,
                    categories = categories.filter { it.type == TransactionType.EXPENSE },
                    existingTransaction = existing,
                    onSaved = { navController.popBackStack() }
                )
            }
        }

        composable(
            Screen.AddIncome.route,
            arguments = listOf(navArgument("transactionId") { nullable = true; defaultValue = "" })
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("transactionId")
            val currentUser by authViewModel.currentUser.collectAsState()
            val accounts by accountViewModel.accounts.collectAsState()
            val categories by categoryViewModel.categories.collectAsState()
            val allTransactions by transactionViewModel.allTransactions.collectAsState()
            val existing = allTransactions.find { it.transactionId == txId }

            currentUser?.let { user ->
                AddTransactionScreen(
                    viewModel = transactionViewModel,
                    uid = user.uid,
                    type = TransactionType.INCOME,
                    accounts = accounts,
                    categories = categories.filter { it.type == TransactionType.INCOME },
                    existingTransaction = existing,
                    onSaved = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AddTransfer.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val accounts by accountViewModel.accounts.collectAsState()
            currentUser?.let { user ->
                AddTransferScreen(
                    viewModel = transactionViewModel,
                    uid = user.uid,
                    accounts = accounts,
                    onSaved = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Transactions.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val filtered by transactionViewModel.filtered.collectAsState()
            val filters by transactionViewModel.filters.collectAsState()

            TransactionsScreen(
                transactions = filtered,
                filters = filters,
                onFiltersChange = { newFilters -> transactionViewModel.updateFilters { newFilters } },
                onEdit = { tx ->
                    val dest = if (tx.type == TransactionType.EXPENSE) Screen.AddExpense.build(tx.transactionId) else Screen.AddIncome.build(tx.transactionId)
                    navController.navigate(dest)
                },
                onDelete = { tx -> currentUser?.let { u -> transactionViewModel.deleteTransaction(u.uid, tx) {} } },
                onDuplicate = { tx ->
                    currentUser?.let { u ->
                        val copy = tx.copy(transactionId = "", title = tx.title + " (copy)", date = System.currentTimeMillis())
                        transactionViewModel.addExpenseOrIncome(u.uid, copy) {}
                    }
                }
            )
        }

        composable(Screen.Accounts.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val accounts by accountViewModel.accounts.collectAsState()

            AccountsScreen(
                accounts = accounts,
                onCreateAccount = { name, type, opening, onResult ->
                    currentUser?.let { u -> accountViewModel.createAccount(u.uid, name, type, opening, onResult) }
                },
                onDeleteAccount = { acc -> currentUser?.let { u -> accountViewModel.deleteAccount(u.uid, acc.accountId) {} } }
            )
        }

        composable(Screen.Budgets.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val month by budgetViewModel.selectedMonth.collectAsState()
            val usage by budgetViewModel.usage.collectAsState()
            val categories by categoryViewModel.categories.collectAsState()
            var showMonthPicker by remember { mutableStateOf(false) }

            BudgetsScreen(
                monthKey = month,
                usage = usage,
                expenseCategories = categories.filter { it.type == TransactionType.EXPENSE },
                onPickMonth = { showMonthPicker = true },
                onSetBudget = { category, limit, onResult ->
                    currentUser?.let { u -> budgetViewModel.setBudget(u.uid, category, limit, onResult) }
                },
                onDeleteBudget = { id -> currentUser?.let { u -> budgetViewModel.deleteBudget(u.uid, id) } }
            )

            if (showMonthPicker) {
                MonthPickerDialog(
                    currentMonth = month,
                    onDismiss = { showMonthPicker = false },
                    onSelect = { m -> currentUser?.let { u -> budgetViewModel.setMonth(u.uid, m) }; showMonthPicker = false }
                )
            }
        }

        composable(Screen.Profile.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            ProfileScreen(
                viewModel = authViewModel,
                user = currentUser,
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }
    }
}

@Composable
private fun MonthPickerDialog(currentMonth: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val months = DateUtils.lastNMonthKeys(12)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month") },
        text = {
            LazyColumn {
                items(months) { m ->
                    TextButton(onClick = { onSelect(m) }) {
                        Text(DateUtils.monthKeyToLabel(m) + if (m == currentMonth) " (current)" else "")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
