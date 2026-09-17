package com.example.financemanager.ui.screens.budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.Category
import com.example.financemanager.ui.theme.ErrorRed
import com.example.financemanager.ui.theme.PositiveGreen
import com.example.financemanager.utils.BudgetUsage
import com.example.financemanager.utils.CurrencyUtils
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.utils.Resource

@Composable
fun BudgetsScreen(
    monthKey: String,
    usage: List<BudgetUsage>,
    expenseCategories: List<Category>,
    onPickMonth: () -> Unit,
    onSetBudget: (category: String, limitPaise: Long, onResult: (Resource<Unit>) -> Unit) -> Unit,
    onDeleteBudget: (budgetId: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Set Budget") })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budgets", style = MaterialTheme.typography.headlineMedium)
                AssistChip(onClick = onPickMonth, label = { Text(DateUtils.monthKeyToLabel(monthKey)) })
            }
            Spacer(Modifier.height(16.dp))

            if (usage.isEmpty()) {
                Text("No budgets set for this month", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(usage, key = { it.budget.budgetId }) { u ->
                        BudgetCard(u, onDelete = { onDeleteBudget(u.budget.budgetId) })
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SetBudgetDialog(
            categories = expenseCategories,
            onDismiss = { showAddDialog = false },
            onSave = { category, limit ->
                onSetBudget(category, limit) { result -> if (result is Resource.Success) showAddDialog = false }
            }
        )
    }
}

@Composable
private fun BudgetCard(usage: BudgetUsage, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(usage.budget.category.ifBlank { "Overall" }, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onDelete) { Text("Remove") }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${CurrencyUtils.formatPaise(usage.spentPaise)} of ${CurrencyUtils.formatPaise(usage.budget.limitPaise)} used",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(6.dp))
            val fraction = (usage.percentUsed / 100f).coerceIn(0f, 1f)
            val barColor = if (usage.percentUsed >= 100) ErrorRed else if (usage.percentUsed >= 75) Color(0xFFF2A93B) else PositiveGreen
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color.LightGray, RoundedCornerShape(4.dp))) {
                Box(modifier = Modifier.fillMaxWidth(fraction).fillMaxSize().background(barColor, RoundedCornerShape(4.dp)))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (usage.remainingPaise >= 0) "${CurrencyUtils.formatPaise(usage.remainingPaise)} remaining (${usage.percentUsed}%)"
                else "Over budget by ${CurrencyUtils.formatPaise(-usage.remainingPaise)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SetBudgetDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (category: String, limitPaise: Long) -> Unit
) {
    var category by remember { mutableStateOf("") } // blank = Overall
    var limit by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val options = listOf("Overall") + categories.map { it.name }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = category.ifBlank { "Overall" }, onValueChange = {}, readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { category = if (opt == "Overall") "" else opt; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = limit, onValueChange = { limit = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monthly Limit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val limitPaise = CurrencyUtils.rupeesToPaise(limit)
                if (limitPaise <= 0) { Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show(); return@TextButton }
                onSave(category, limitPaise)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
