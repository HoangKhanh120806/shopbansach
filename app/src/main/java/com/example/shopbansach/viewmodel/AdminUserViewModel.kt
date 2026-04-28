package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUserUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class AdminUserViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    
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
                _uiState.update { it.copy(
                    users = userList, 
                    filteredUsers = filterList(userList, _uiState.value.searchQuery),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredUsers = filterList(it.users, query)
            ) 
        }
    }

    private fun filterList(users: List<User>, query: String): List<User> {
        if (query.isEmpty()) return users
        return users.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.email.contains(query, ignoreCase = true) 
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

    /**
     * Admin xóa người dùng (Lưu ý: Chỉ xóa được data trong Firestore, 
     * việc xóa Auth User cần Cloud Functions hoặc Admin SDK, 
     * nhưng ở mức độ App này chúng ta sẽ xóa data trước)
     */
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteUserByAdmin(userId)
            if (result.isSuccess) {
                loadUsers()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Không thể xóa người dùng") }
            }
        }
    }
}
