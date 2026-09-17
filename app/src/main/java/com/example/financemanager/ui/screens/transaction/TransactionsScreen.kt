package com.example.financemanager.ui.screens.transaction

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.utils.CurrencyUtils
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.viewmodel.SortOrder
import com.example.financemanager.viewmodel.TransactionFilters
import com.example.financemanager.viewmodel.TypeFilter

@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    filters: TransactionFilters,
    onFiltersChange: (TransactionFilters) -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onDuplicate: (Transaction) -> Unit
) {
    var toDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transactions", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = filters.searchQuery,
            onValueChange = { onFiltersChange(filters.copy(searchQuery = it)) },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TypeFilter.entries.toList()) { tf ->
                FilterChip(
                    selected = filters.typeFilter == tf,
                    onClick = { onFiltersChange(filters.copy(typeFilter = tf)) },
                    label = { Text(tf.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SortOrder.entries.toList()) { order ->
                FilterChip(
                    selected = filters.sortOrder == order,
                    onClick = { onFiltersChange(filters.copy(sortOrder = order)) },
                    label = { Text(order.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Text("No transactions found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(transactions, key = { it.transactionId }) { tx ->
                    TransactionListItem(
                        tx = tx,
                        onEdit = { onEdit(tx) },
                        onDeleteRequest = { toDelete = tx },
                        onDuplicate = { onDuplicate(tx) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    toDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("\"${tx.title}\" will be removed and the account balance recalculated.") },
            confirmButton = { TextButton(onClick = { onDelete(tx); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun TransactionListItem(
    tx: Transaction,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDuplicate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.titleMedium)
                val subtitle = when (tx.type) {
                    TransactionType.TRANSFER -> "${tx.fromAccountName} → ${tx.toAccountName} · ${DateUtils.formatDate(tx.date)}"
                    else -> "${tx.category} · ${tx.accountName} · ${DateUtils.formatDate(tx.date)}"
                }
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            val sign = when (tx.type) { TransactionType.INCOME -> "+"; TransactionType.EXPENSE -> "-"; else -> "" }
            Text("$sign${CurrencyUtils.formatPaise(tx.amountPaise)}", style = MaterialTheme.typography.titleMedium)
            if (tx.type != TransactionType.TRANSFER) {
                IconButton(onClick = onEdit) { androidx.compose.material3.Icon(Icons.Filled.Edit, "Edit") }
            }
            IconButton(onClick = onDeleteRequest) { androidx.compose.material3.Icon(Icons.Filled.Delete, "Delete") }
        }
    }
}
