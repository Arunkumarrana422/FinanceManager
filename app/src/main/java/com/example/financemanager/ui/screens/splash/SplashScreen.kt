package com.example.financemanager.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financemanager.ui.theme.BluePrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(isLoggedIn: Boolean, onNavigateNext: (loggedIn: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        onNavigateNext(isLoggedIn)
    }
    Box(modifier = Modifier.fillMaxSize().background(BluePrimary), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.padding(bottom = 16.dp))
            Text("Expense Manager", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
            Text("Know where every rupee goes", color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(top = 8.dp))
        }
    }
}
