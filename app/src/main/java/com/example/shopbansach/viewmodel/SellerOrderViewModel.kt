package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Notification
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.NotificationRepository
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
    private val orderRepository: OrderRepository = OrderRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
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
            
            // 1. Lấy thông tin đơn hàng trước để biết userId người mua
            val order = orderRepository.getOrderById(orderId)
            
            // 2. Cập nhật trạng thái
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(successMessage = "Cập nhật thành công: $newStatus", isLoading = false) }
                
                // 3. Gửi thông báo cho người mua
                if (order != null) {
                    val notification = Notification(
                        userId = order.userId,
                        title = "Cập nhật đơn hàng",
                        message = "Đơn hàng #${orderId.takeLast(6).uppercase()} của bạn đã chuyển sang trạng thái: $newStatus",
                        type = "ORDER",
                        orderId = orderId
                    )
                    notificationRepository.createNotification(notification)
                }
                
                loadOrders()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: $errorMsg") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
