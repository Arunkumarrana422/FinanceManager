package com.example.financemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.TransactionType
import com.example.financemanager.repository.TransactionRepository
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOrder { NEWEST, OLDEST, HIGHEST, LOWEST }
enum class TypeFilter { ALL, EXPENSE, INCOME, TRANSFER }

data class TransactionFilters(
    val searchQuery: String = "",
    val typeFilter: TypeFilter = TypeFilter.ALL,
    val category: String? = null,
    val accountId: String? = null,
    val monthKey: String? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

class TransactionViewModel(
    private val repository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions.asStateFlow()

    private val _filters = MutableStateFlow(TransactionFilters())
    val filters: StateFlow<TransactionFilters> = _filters.asStateFlow()

    private val _filtered = MutableStateFlow<List<Transaction>>(emptyList())
    val filtered: StateFlow<List<Transaction>> = _filtered.asStateFlow()

    fun observeTransactions(uid: String) {
        viewModelScope.launch {
            repository.observeTransactions(uid).collect {
                _allTransactions.value = it
                applyFilters()
            }
        }
    }

    fun updateFilters(update: (TransactionFilters) -> TransactionFilters) {
        _filters.value = update(_filters.value)
        applyFilters()
    }

    private fun applyFilters() {
        val f = _filters.value
        var list = _allTransactions.value

        if (f.typeFilter != TypeFilter.ALL) {
            list = list.filter { it.type == f.typeFilter.name }
        }
        if (f.searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(f.searchQuery, true) ||
                    it.notes.contains(f.searchQuery, true) ||
                    it.category.contains(f.searchQuery, true)
            }
        }
        f.category?.let { cat -> list = list.filter { it.category == cat } }
        f.accountId?.let { accId -> list = list.filter { it.accountId == accId || it.fromAccountId == accId || it.toAccountId == accId } }
        f.monthKey?.let { month -> list = list.filter { DateUtils.monthKey(it.date) == month } }

        list = when (f.sortOrder) {
            SortOrder.NEWEST -> list.sortedByDescending { it.date }
            SortOrder.OLDEST -> list.sortedBy { it.date }
            SortOrder.HIGHEST -> list.sortedByDescending { it.amountPaise }
            SortOrder.LOWEST -> list.sortedBy { it.amountPaise }
        }
        _filtered.value = list
    }

    fun addExpenseOrIncome(uid: String, transaction: Transaction, onResult: (Resource<Transaction>) -> Unit) {
        if (transaction.title.isBlank()) { onResult(Resource.Error("Please enter a title")); return }
        if (transaction.amountPaise <= 0) { onResult(Resource.Error("Amount must be greater than zero")); return }
        if (transaction.accountId.isBlank()) { onResult(Resource.Error("Please select an account")); return }
        if (transaction.category.isBlank()) { onResult(Resource.Error("Please select a category")); return }
        viewModelScope.launch { onResult(repository.addTransaction(uid, transaction)) }
    }

    fun addTransfer(uid: String, transaction: Transaction, onResult: (Resource<Transaction>) -> Unit) {
        if (transaction.amountPaise <= 0) { onResult(Resource.Error("Amount must be greater than zero")); return }
        if (transaction.fromAccountId.isBlank() || transaction.toAccountId.isBlank()) {
            onResult(Resource.Error("Please select both accounts")); return
        }
        if (transaction.fromAccountId == transaction.toAccountId) {
            onResult(Resource.Error("Choose two different accounts")); return
        }
        val toSave = transaction.copy(type = TransactionType.TRANSFER, title = transaction.title.ifBlank { "Transfer" })
        viewModelScope.launch { onResult(repository.addTransaction(uid, toSave)) }
    }

    fun updateTransaction(uid: String, old: Transaction, updated: Transaction, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch { onResult(repository.updateTransaction(uid, old, updated)) }
    }

    fun deleteTransaction(uid: String, transaction: Transaction, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch { onResult(repository.deleteTransaction(uid, transaction)) }
    }
}
