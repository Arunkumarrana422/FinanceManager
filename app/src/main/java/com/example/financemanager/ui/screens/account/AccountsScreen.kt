package com.example.financemanager.ui.screens.account

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.Account
import com.example.financemanager.model.AccountType
import com.example.financemanager.utils.CurrencyUtils
import com.example.financemanager.utils.Resource

@Composable
fun AccountsScreen(
    accounts: List<Account>,
    onCreateAccount: (name: String, type: String, openingBalancePaise: Long, onResult: (Resource<Account>) -> Unit) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Add Account") })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Accounts", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Total: ${CurrencyUtils.formatPaise(accounts.sumOf { it.currentBalancePaise })}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))

            if (accounts.isEmpty()) {
                Text("No accounts yet — add Cash, Bank, or UPI to start tracking", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(accounts, key = { it.accountId }) { acc ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(acc.name, style = MaterialTheme.typography.titleMedium)
                                    Text(acc.type, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(CurrencyUtils.formatPaise(acc.currentBalancePaise), style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = { toDelete = acc }) { Icon(Icons.Filled.Delete, "Delete") }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { name, type, opening ->
                onCreateAccount(name, type, opening) { result ->
                    if (result is Resource.Success) showAddDialog = false
                }
            }
        )
    }

    toDelete?.let { acc ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete ${acc.name}?") },
            text = { Text("Transactions on this account will remain but will no longer show a valid account.") },
            confirmButton = { TextButton(onClick = { onDeleteAccount(acc); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, openingBalancePaise: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.CASH) }
    var opening by remember { mutableStateOf("0") }
    var typeExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Account Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = type, onValueChange = {}, readOnly = true, label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        AccountType.ALL.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = opening, onValueChange = { opening = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Opening Balance (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { Toast.makeText(context, "Enter an account name", Toast.LENGTH_SHORT).show(); return@TextButton }
                onCreate(name.trim(), type, CurrencyUtils.rupeesToPaise(opening))
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
