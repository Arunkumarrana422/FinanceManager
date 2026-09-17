package com.example.financemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.model.Category
import com.example.financemanager.repository.CategoryRepository
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    fun observeCategories(uid: String) {
        viewModelScope.launch { repository.observeCategories(uid).collect { _categories.value = it } }
    }

    fun addCategory(uid: String, name: String, type: String, onResult: (Resource<Category>) -> Unit) {
        if (name.isBlank()) { onResult(Resource.Error("Category name cannot be empty")); return }
        viewModelScope.launch { onResult(repository.addCategory(uid, name.trim(), type)) }
    }

    fun deleteCategory(uid: String, category: Category, hasTransactions: Boolean, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch { onResult(repository.deleteCategory(uid, category.categoryId, hasTransactions)) }
    }
}
