package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellerOrderUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SellerOrderViewModel(
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SellerOrderUiState())
    val uiState: StateFlow<SellerOrderUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val orders = orderRepository.getOrdersBySeller(userId)
                _uiState.update { it.copy(orders = orders, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            if (result.isSuccess) {
                _uiState.update { it.copy(successMessage = "Cập nhật thành công: $newStatus", isLoading = false) }
                loadOrders()
            } else {
                // Hiện lỗi thật từ Firebase để debug
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: $errorMsg") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
