package com.example.financemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.model.User
import com.example.financemanager.repository.AuthRepository
import com.example.financemanager.repository.CategoryRepository
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<User>?>(null)
    val authState: StateFlow<Resource<User>?> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun loadCurrentUserProfile() {
        val uid = repository.currentUser()?.uid ?: return
        viewModelScope.launch {
            when (val result = repository.fetchUserProfile(uid)) {
                is Resource.Success -> _currentUser.value = result.data
                else -> Unit
            }
            categoryRepository.seedDefaultCategoriesIfEmpty(uid)
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields"); return
        }
        if (password.length < 6) {
            _authState.value = Resource.Error("Password must be at least 6 characters"); return
        }
        _authState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.signUp(name, email, password)
            _authState.value = result
            if (result is Resource.Success) {
                _currentUser.value = result.data
                categoryRepository.seedDefaultCategoriesIfEmpty(result.data.uid)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Please enter email and password"); return
        }
        _authState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.login(email, password)
            _authState.value = result
            if (result is Resource.Success) _currentUser.value = result.data
        }
    }

    fun sendPasswordReset(email: String, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch { onResult(repository.sendPasswordReset(email)) }
    }

    fun updateProfile(name: String, currency: String, onResult: (Resource<Unit>) -> Unit) {
        val uid = repository.currentUser()?.uid ?: return
        viewModelScope.launch {
            val result = repository.updateProfile(uid, name, currency)
            if (result is Resource.Success) loadCurrentUserProfile()
            onResult(result)
        }
    }

    fun changePassword(newPassword: String, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch { onResult(repository.changePassword(newPassword)) }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = null
    }

    fun resetAuthState() { _authState.value = null }
}
