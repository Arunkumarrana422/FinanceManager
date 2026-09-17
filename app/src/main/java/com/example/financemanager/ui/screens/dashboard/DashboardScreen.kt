package com.example.financemanager.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.utils.CurrencyUtils
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.utils.PeriodSummary

@Composable
fun DashboardScreen(
    todaySummary: PeriodSummary,
    monthSummary: PeriodSummary,
    remainingBudget: Long?,
    recentTransactions: List<Transaction>,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddTransfer: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) { Icon(Icons.Filled.Add, "Add Expense") }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = onOpenTransactions, icon = { Icon(Icons.Filled.SwapHoriz, null) }, label = { Text("Transactions") })
                NavigationBarItem(selected = false, onClick = onOpenBudgets, icon = { Icon(Icons.Filled.Assessment, null) }, label = { Text("Budgets") })
                NavigationBarItem(selected = false, onClick = onOpenAccounts, icon = { Icon(Icons.Filled.AccountBalance, null) }, label = { Text("Accounts") })
                NavigationBarItem(selected = false, onClick = onOpenProfile, icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {

            item {
                Text("Today", style = MaterialTheme.typography.bodyMedium)
                Text(CurrencyUtils.formatPaise(todaySummary.net), style = MaterialTheme.typography.headlineMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Income: ${CurrencyUtils.formatPaise(todaySummary.income)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Expense: ${CurrencyUtils.formatPaise(todaySummary.expense)}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("This Month", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DashboardStat("Income", CurrencyUtils.formatPaise(monthSummary.income))
                            DashboardStat("Expenses", CurrencyUtils.formatPaise(monthSummary.expense))
                            DashboardStat("Savings", CurrencyUtils.formatPaise(monthSummary.net))
                        }
                        if (remainingBudget != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Remaining budget: ${CurrencyUtils.formatPaise(remainingBudget)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickAction("Expense", Icons.Filled.Remove, Modifier.weight(1f), onAddExpense)
                    QuickAction("Income", Icons.Filled.Add, Modifier.weight(1f), onAddIncome)
                    QuickAction("Transfer", Icons.Filled.SwapHoriz, Modifier.weight(1f), onAddTransfer)
                }
                Spacer(Modifier.height(20.dp))
                Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            if (recentTransactions.isEmpty()) {
                item { Text("No transactions yet — add your first expense or income", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(recentTransactions.take(10), key = { it.transactionId }) { tx ->
                    TransactionRow(tx)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(tx.title, style = MaterialTheme.typography.titleMedium)
                val subtitle = when (tx.type) {
                    TransactionType.TRANSFER -> "${tx.fromAccountName} → ${tx.toAccountName} · ${DateUtils.formatDate(tx.date)}"
                    else -> "${tx.category} · ${tx.accountName} · ${DateUtils.formatDate(tx.date)}"
                }
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            val sign = when (tx.type) {
                TransactionType.INCOME -> "+"
                TransactionType.EXPENSE -> "-"
                else -> ""
            }
            Text("$sign${CurrencyUtils.formatPaise(tx.amountPaise)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
