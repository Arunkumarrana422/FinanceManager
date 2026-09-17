package com.example.financemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.model.Budget
import com.example.financemanager.model.Transaction
import com.example.financemanager.repository.BudgetRepository
import com.example.financemanager.utils.BudgetUsage
import com.example.financemanager.utils.DateUtils
import com.example.financemanager.utils.FinanceCalculator
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(DateUtils.currentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _usage = MutableStateFlow<List<BudgetUsage>>(emptyList())
    val usage: StateFlow<List<BudgetUsage>> = _usage.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var currentUid: String? = null

    fun start(uid: String, transactions: kotlinx.coroutines.flow.StateFlow<List<Transaction>>) {
        currentUid = uid
        viewModelScope.launch {
            repository.observeBudgets(uid, _selectedMonth.value).collect {
                _budgets.value = it
                recompute()
            }
        }
        viewModelScope.launch {
            transactions.collect {
                allTransactions = it
                recompute()
            }
        }
    }

    fun setMonth(uid: String, month: String) {
        _selectedMonth.value = month
        viewModelScope.launch {
            repository.observeBudgets(uid, month).collect {
                _budgets.value = it
                recompute()
            }
        }
    }

    private fun recompute() {
        val monthTx = allTransactions.filter { DateUtils.monthKey(it.date) == _selectedMonth.value }
        _usage.value = FinanceCalculator.budgetUsage(_budgets.value, monthTx)
    }

    fun setBudget(uid: String, category: String, limitPaise: Long, onResult: (Resource<Unit>) -> Unit) {
        if (limitPaise <= 0) { onResult(Resource.Error("Budget limit must be greater than zero")); return }
        viewModelScope.launch { onResult(repository.setBudget(uid, category, _selectedMonth.value, limitPaise)) }
    }

    fun deleteBudget(uid: String, budgetId: String) {
        viewModelScope.launch { repository.deleteBudget(uid, budgetId) }
    }
}
