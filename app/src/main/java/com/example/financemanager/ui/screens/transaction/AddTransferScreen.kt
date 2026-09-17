package com.example.financemanager.ui.screens.transaction

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.financemanager.model.Transaction
import com.example.financemanager.utils.CurrencyUtils
import com.example.financemanager.utils.Resource
import com.example.financemanager.viewmodel.TransactionViewModel

@Composable
fun AddTransferScreen(
    viewModel: TransactionViewModel,
    uid: String,
    accounts: List<Account>,
    onSaved: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var fromId by remember { mutableStateOf(accounts.getOrNull(0)?.accountId ?: "") }
    var toId by remember { mutableStateOf(accounts.getOrNull(1)?.accountId ?: accounts.getOrNull(0)?.accountId ?: "") }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Transfer Money", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        val fromName = accounts.find { it.accountId == fromId }?.name ?: ""
        ExposedDropdownMenuBox(expanded = fromExpanded, onExpandedChange = { fromExpanded = it }) {
            OutlinedTextField(
                value = fromName, onValueChange = {}, readOnly = true, label = { Text("From Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                accounts.forEach { acc -> DropdownMenuItem(text = { Text(acc.name) }, onClick = { fromId = acc.accountId; fromExpanded = false }) }
            }
        }
        Spacer(Modifier.height(12.dp))

        val toName = accounts.find { it.accountId == toId }?.name ?: ""
        ExposedDropdownMenuBox(expanded = toExpanded, onExpandedChange = { toExpanded = it }) {
            OutlinedTextField(
                value = toName, onValueChange = {}, readOnly = true, label = { Text("To Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                accounts.forEach { acc -> DropdownMenuItem(text = { Text(acc.name) }, onClick = { toId = acc.accountId; toExpanded = false }) }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val fromAcc = accounts.find { it.accountId == fromId }
                val toAcc = accounts.find { it.accountId == toId }
                val transaction = Transaction(
                    amountPaise = CurrencyUtils.rupeesToPaise(amount),
                    fromAccountId = fromId, fromAccountName = fromAcc?.name ?: "",
                    toAccountId = toId, toAccountName = toAcc?.name ?: "",
                    notes = notes.trim()
                )
                saving = true
                viewModel.addTransfer(uid, transaction) { result ->
                    saving = false
                    if (result is Resource.Success) onSaved()
                    else if (result is Resource.Error) Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (saving) "Transferring..." else "Transfer") }
    }
}
