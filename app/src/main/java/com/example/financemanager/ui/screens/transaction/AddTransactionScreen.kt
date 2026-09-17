package com.example.financemanager.ui.screens.transaction

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.Account
import com.example.financemanager.model.Category
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.utils.CurrencyUtils
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.utils.Resource
import com.example.financemanager.viewmodel.TransactionViewModel

/**
 * Shared form for both Add Expense and Add Income — `type` decides the
 * category list, label wording, and which repository call is made on save.
 */
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    uid: String,
    type: String, // TransactionType.EXPENSE or TransactionType.INCOME
    accounts: List<Account>,
    categories: List<Category>, // pre-filtered to this type by the caller
    existingTransaction: Transaction? = null,
    onSaved: () -> Unit
) {
    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var amount by remember {
        mutableStateOf(existingTransaction?.let {
            val d = CurrencyUtils.paiseToRupeesDouble(it.amountPaise)
            if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
        } ?: "")
    }
    var category by remember { mutableStateOf(existingTransaction?.category ?: categories.firstOrNull()?.name ?: "") }
    var accountId by remember { mutableStateOf(existingTransaction?.accountId ?: accounts.firstOrNull()?.accountId ?: "") }
    var notes by remember { mutableStateOf(existingTransaction?.notes ?: "") }
    var date by remember { mutableStateOf(existingTransaction?.date ?: System.currentTimeMillis()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isExpense = type == TransactionType.EXPENSE

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text(
            if (existingTransaction == null) (if (isExpense) "Add Expense" else "Add Income") else "Edit Transaction",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
            OutlinedTextField(
                value = category, onValueChange = {}, readOnly = true, label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name) }, onClick = { category = cat.name; categoryExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val accountName = accounts.find { it.accountId == accountId }?.name ?: ""
        ExposedDropdownMenuBox(expanded = accountExpanded, onExpandedChange = { accountExpanded = it }) {
            OutlinedTextField(
                value = accountName, onValueChange = {}, readOnly = true, label = { Text("Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            DropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                accounts.forEach { acc ->
                    DropdownMenuItem(text = { Text("${acc.name} (${CurrencyUtils.formatPaise(acc.currentBalancePaise)})") }, onClick = { accountId = acc.accountId; accountExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Text("Date: ${DateUtils.formatDate(date)}", style = MaterialTheme.typography.bodyMedium)
        Text(
            "(Wire a DatePickerDialog from the hosting Activity to update `date` — left as a callback hook.)",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (accounts.isEmpty()) {
                    Toast.makeText(context, "Add an account first", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val account = accounts.find { it.accountId == accountId }
                val transaction = Transaction(
                    transactionId = existingTransaction?.transactionId ?: "",
                    userId = uid,
                    type = type,
                    amountPaise = CurrencyUtils.rupeesToPaise(amount),
                    title = title.trim(),
                    category = category,
                    accountId = accountId,
                    accountName = account?.name ?: "",
                    notes = notes.trim(),
                    date = date,
                    createdAt = existingTransaction?.createdAt ?: System.currentTimeMillis()
                )
                saving = true
                if (existingTransaction == null) {
                    viewModel.addExpenseOrIncome(uid, transaction) { result ->
                        saving = false
                        if (result is Resource.Success) onSaved()
                        else if (result is Resource.Error) Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.updateTransaction(uid, existingTransaction, transaction) { result ->
                        saving = false
                        if (result is Resource.Success) onSaved()
                        else if (result is Resource.Error) Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (saving) "Saving..." else "Save") }
    }
}
