package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminOrderUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccess: String? = null
)

class AdminOrderViewModel(private val repository: OrderRepository = OrderRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init {
        loadAllOrders()
    }

    fun loadAllOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val allOrders = repository.getAllOrders()
                _uiState.update { it.copy(orders = allOrders, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateOrderStatus(orderId, newStatus)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccess = "Đã cập nhật trạng thái đơn hàng") }
                loadAllOrders()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi cập nhật: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccess = null) }
    }
}
