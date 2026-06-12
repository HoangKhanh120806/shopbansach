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
                
                val revenue = orders.filter { it.status == "Hoàn thành" }.sumOf { it.totalPrice }
                val orderCount = orders.size
                val bookCount = books.size
                val userCount = users.size
                
                val statusMap = orders.groupingBy { it.status }.eachCount()
                
                val bookSales = mutableMapOf<String, Int>()
                orders.forEach { order ->
                    order.items.forEach { item ->
                        bookSales[item.title] = (bookSales[item.title] ?: 0) + item.quantity
                    }
                }
                val topBooks = bookSales.toList().sortedByDescending { it.second }.take(5)

                _uiState.update { it.copy(
                    totalRevenue = revenue,
                    totalOrders = orderCount,
                    totalBooks = bookCount,
                    totalUsers = userCount,
                    ordersByStatus = statusMap,
                    topSellingBooks = topBooks,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
