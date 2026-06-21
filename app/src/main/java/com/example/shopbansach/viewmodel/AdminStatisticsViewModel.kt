package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.OrderRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.example.shopbansach.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminStatisticsUiState(
    val totalRevenue: Long = 0,
    val totalOrders: Int = 0,
    val totalBooks: Int = 0,
    val totalUsers: Int = 0,
    val ordersByStatus: Map<String, Int> = emptyMap(),
    val topSellingBooks: List<Pair<String, Int>> = emptyList(), // Book Title to Quantity
    val revenueByShop: List<Pair<String, Long>> = emptyList(), // Shop Name to Revenue
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminStatisticsViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStatisticsUiState())
    val uiState: StateFlow<AdminStatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val orders = orderRepository.getAllOrders()
                val books = bookRepository.getAllBooks(limit = 1000)
                val users = authRepository.getAllUsers()
                
                // Thống kê doanh thu (Chỉ tính đơn hàng thành công)
                val completedStatuses = listOf("đã giao", "hoàn thành", "thành công")
                val revenue = orders.filter { it.status.trim().lowercase() in completedStatuses }.sumOf { it.totalPrice }
                val orderCount = orders.size
                val bookCount = books.size
                val userCount = users.size
                
                val statusMap = orders.groupingBy { it.status }.eachCount()
                
                val bookSales = mutableMapOf<String, Int>()
                val shopRevenueMap = mutableMapOf<String, Long>()
                
                // Tạo bản đồ userId -> ShopName để tra cứu nhanh
                val userIdToShopName = users.associate { it.id to (it.shopName ?: it.name) }

                orders.forEach { order ->
                    val status = order.status.trim().lowercase()
                    // Không tính đơn hủy vào sách bán chạy để số liệu chính xác
                    if (status != "hủy" && status != "đã hủy") {
                        order.items.forEach { item ->
                            // Thống kê sách bán chạy
                            bookSales[item.title] = (bookSales[item.title] ?: 0) + item.quantity
                            
                            // Thống kê doanh thu theo shop (Chỉ tính đơn thành công)
                            if (status in completedStatuses) {
                                val shopName = if (item.ownerId.isNotEmpty()) {
                                    userIdToShopName[item.ownerId] ?: "Shop ẩn"
                                } else {
                                    "Chưa xác định"
                                }
                                shopRevenueMap[shopName] = (shopRevenueMap[shopName] ?: 0L) + (item.price * item.quantity)
                            }
                        }
                    }
                }
                
                val topBooks = bookSales.toList().sortedByDescending { it.second }.take(10)
                val topShops = shopRevenueMap.toList().sortedByDescending { it.second }

                _uiState.update { it.copy(
                    totalRevenue = revenue,
                    totalOrders = orderCount,
                    totalBooks = bookCount,
                    totalUsers = userCount,
                    ordersByStatus = statusMap,
                    topSellingBooks = topBooks,
                    revenueByShop = topShops,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
