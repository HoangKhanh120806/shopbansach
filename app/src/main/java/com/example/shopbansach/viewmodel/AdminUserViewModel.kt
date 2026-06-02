package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Notification
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.NotificationRepository
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
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class AdminUserViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
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
                _uiState.update { it.copy(
                    users = userList, 
                    filteredUsers = filterList(userList, _uiState.value.searchQuery),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Không thể tải danh sách: ${e.message}") }
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
            it.email.contains(query, ignoreCase = true) ||
            (it.shopName?.contains(query, ignoreCase = true) ?: false)
        }
    }

    fun changeUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateUserRole(userId, newRole)
            if (result.isSuccess) {
                // TẠO THÔNG BÁO KHI DUYỆT NGƯỜI BÁN
                if (newRole == UserRole.SELLER) {
                    notificationRepository.createNotification(
                        Notification(
                            userId = userId,
                            title = "Chúc mừng!",
                            message = "Yêu cầu bán hàng của bạn đã được Admin phê duyệt. Bạn có thể đăng bán sách ngay bây giờ."
                        )
                    )
                } else if (newRole == UserRole.USER) {
                    notificationRepository.createNotification(
                        Notification(
                            userId = userId,
                            title = "Cập nhật tài khoản",
                            message = "Quyền hạn của bạn đã được thay đổi thành Người dùng thường."
                        )
                    )
                }

                _uiState.update { it.copy(actionSuccessMessage = "Đã cập nhật quyền thành ${newRole.name}") }
                loadUsers() 
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi đổi quyền: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun updateUserInfo(userId: String, newName: String, newShopName: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.updateUserProfile(userId, newName)
                if (newShopName != null) {
                    repository.updateShopName(userId, newShopName)
                }
                _uiState.update { it.copy(actionSuccessMessage = "Đã cập nhật thông tin thành công") }
                loadUsers()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi cập nhật: ${e.message}") }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteUserByAdmin(userId)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccessMessage = "Đã xóa người dùng thành công") }
                loadUsers()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi khi xóa: ${result.exceptionOrNull()?.message}") }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
