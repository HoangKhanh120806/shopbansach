package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.CloudinaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminUserViewModel(
    private val cloudinaryRepository: CloudinaryRepository,
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminUserUiState())
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val userList = repository.getAllUsers()
                _uiState.update { it.copy(users = userList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun changeUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            val result = repository.updateUserRole(userId, newRole)
            if (result.isSuccess) {
                loadUsers() 
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            // Truyền cloudinaryRepository để dọn dẹp ảnh bìa sách của user bị xóa
            val result = repository.deleteUserByAdmin(userId, cloudinaryRepository)
            if (result.isSuccess) {
                loadUsers()
            }
        }
    }
}
