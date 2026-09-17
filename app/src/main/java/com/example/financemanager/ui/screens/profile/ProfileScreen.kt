package com.example.financemanager.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.User
import com.example.financemanager.utils.Resource
import com.example.financemanager.viewmodel.AuthViewModel

private val SUPPORTED_CURRENCIES = listOf("INR", "USD", "EUR", "GBP")

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    user: User?,
    onLogout: () -> Unit
) {
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var currency by remember(user) { mutableStateOf(user?.currency ?: "INR") }
    var newPassword by remember { mutableStateOf("") }
    var currencyExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = user?.email ?: "", onValueChange = {}, readOnly = true, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = it }) {
            OutlinedTextField(
                value = currency, onValueChange = {}, readOnly = true, label = { Text("Currency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                SUPPORTED_CURRENCIES.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { currency = c; currencyExpanded = false }) }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                viewModel.updateProfile(name.trim(), currency) { result ->
                    val msg = if (result is Resource.Success) "Profile updated" else (result as? Resource.Error)?.message ?: "Failed"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Profile") }

        Spacer(Modifier.height(28.dp))
        Text("Change Password", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                viewModel.changePassword(newPassword) { result ->
                    val msg = if (result is Resource.Success) "Password changed" else (result as? Resource.Error)?.message ?: "Failed"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (result is Resource.Success) newPassword = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Update Password") }

        Spacer(Modifier.height(28.dp))
        Button(onClick = { viewModel.logout(); onLogout() }, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}
