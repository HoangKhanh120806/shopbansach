package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Notification
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.NotificationRepository
import com.example.shopbansach.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminOrderUiState(
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class AdminOrderViewModel(
    private val repository: OrderRepository = OrderRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val orderList = repository.getAllOrders()
                _uiState.update { it.copy(
                    orders = orderList, 
                    filteredOrders = filterList(orderList, _uiState.value.searchQuery),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val order = repository.getOrderById(orderId)
            val result = repository.updateOrderStatus(orderId, newStatus)
            
            if (result.isSuccess) {
                if (order != null) {
                    val statusClean = newStatus.trim()
                    val firstItemName = order.items.firstOrNull()?.title ?: "sản phẩm"
                    val moreItems = if (order.items.size > 1) " và ${order.items.size - 1} sản phẩm khác" else ""

                    val notificationTitle = when {
                        statusClean.contains("Đang giao") -> "🚚 Đơn hàng đang đến với bạn"
                        statusClean.contains("Đã giao") || statusClean.contains("thành công") -> "✅ Giao hàng thành công"
                        statusClean.contains("Hủy") -> "❌ Đơn hàng đã bị hủy"
                        else -> "📦 Cập nhật đơn hàng"
                    }

                    val notificationMessage = when {
                        statusClean.contains("Đang giao") -> 
                            "Cuốn sách '$firstItemName'$moreItems đã được gửi đi. Bạn vui lòng chú ý điện thoại nhé!"
                        statusClean.contains("Đã giao") || statusClean.contains("thành công") -> 
                            "Đơn hàng có '$firstItemName' đã giao thành công. Bạn đánh giá sản phẩm để nhận xu nhé! ❤️"
                        statusClean.contains("Hủy") -> 
                            "Rất tiếc, đơn hàng #${orderId.takeLast(6).uppercase()} đã bị hủy bởi hệ thống."
                        else -> "Đơn hàng #${orderId.takeLast(6).uppercase()} của bạn đã chuyển sang trạng thái: $newStatus"
                    }

                    notificationRepository.createNotification(
                        Notification(
                            userId = order.userId,
                            title = notificationTitle,
                            message = notificationMessage,
                            type = "ORDER",
                            orderId = orderId
                        )
                    )
                }
                _uiState.update { it.copy(actionSuccessMessage = "Đã cập nhật trạng thái đơn hàng", isLoading = false) }
                loadOrders()
            }
 else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredOrders = filterList(it.orders, query)
            ) 
        }
    }

    private fun filterList(orders: List<Order>, query: String): List<Order> {
        if (query.isEmpty()) return orders
        return orders.filter { 
            it.id.contains(query, ignoreCase = true) || 
            it.status.contains(query, ignoreCase = true)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
