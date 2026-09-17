package com.example.financemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.model.Account
import com.example.financemanager.repository.AccountRepository
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    fun observeAccounts(uid: String) {
        viewModelScope.launch { repository.observeAccounts(uid).collect { _accounts.value = it } }
    }

    fun createAccount(uid: String, name: String, type: String, openingBalance: Long, onResult: (Resource<Account>) -> Unit) {
        if (name.isBlank()) { onResult(Resource.Error("Account name cannot be empty")); return }
        viewModelScope.launch { onResult(repository.createAccount(uid, name.trim(), type, openingBalance)) }
    }

    fun deleteAccount(uid: String, accountId: String, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch { onResult(repository.deleteAccount(uid, accountId)) }
    }
}
