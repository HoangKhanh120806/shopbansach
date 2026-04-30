package com.example.shopbansach.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.example.shopbansach.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyShopUiState(
    val myBooks: List<Book> = emptyList(),
    val currentUser: User? = null,
    val totalRevenue: Long = 0,
    val totalSold: Int = 0,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null
)

class MyShopViewModel(
    private val cloudinaryRepository: CloudinaryRepository,
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyShopUiState())
    val uiState: StateFlow<MyShopUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()

    init {
        loadData()
    }

    fun loadData() {
        loadUserData()
        loadMyShopData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun loadMyShopData() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Lấy danh sách sách của shop
                val books = bookRepository.getBooksByOwner(userId)
                val myBookIds = books.map { it.id }.toSet()

                // 2. Lấy đơn hàng (Cố gắng lấy từ cả 2 nguồn để đảm bảo không sót)
                val allOrders = mutableListOf<Order>()
                try {
                    allOrders.addAll(orderRepository.getAllOrders())
                } catch (e: Exception) {
                    Log.e("MyShopViewModel", "Error fetching all orders, trying user orders")
                }
                
                // Luôn lấy thêm đơn hàng của chính user (quan trọng khi user tự mua hàng để test)
                val userOrders = orderRepository.getOrdersByUser(userId)
                allOrders.addAll(userOrders)
                
                // Loại bỏ các đơn hàng trùng lặp
                val distinctOrders = allOrders.distinctBy { it.id }
                
                var revenue = 0L
                var soldCount = 0
                
                // 3. Tính toán doanh thu
                distinctOrders.forEach { order ->
                    // Chấp nhận tất cả trạng thái trừ "Đã hủy"
                    if (order.status.trim().lowercase() != "đã hủy") {
                        order.items.forEach { item ->
                            // KIỂM TRA THÔNG MINH: 
                            // Nếu ownerId khớp HOẶC (ownerId trống VÀ bookId nằm trong danh sách sách của tôi)
                            if (item.ownerId == userId || (item.ownerId.isEmpty() && myBookIds.contains(item.bookId))) {
                                revenue += item.price * item.quantity
                                soldCount += item.quantity
                            }
                        }
                    }
                }
                
                _uiState.update { it.copy(
                    myBooks = books,
                    totalRevenue = revenue,
                    totalSold = soldCount,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateShopName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            val result = authRepository.updateShopName(userId, newName)
            if (result.isSuccess) {
                loadUserData()
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }

    fun updateShopAvatar(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            val uploadResult = cloudinaryRepository.uploadImage(imageUri, "accound")
            if (uploadResult.isSuccess) {
                val avatarUrl = uploadResult.getOrNull()
                if (avatarUrl != null) {
                    val updateResult = authRepository.updateShopAvatarUrl(userId, avatarUrl)
                    if (updateResult.isSuccess) {
                        loadUserData()
                    }
                }
            } else {
                 _uiState.update { it.copy(errorMessage = "Lỗi upload: ${uploadResult.exceptionOrNull()?.message}") }
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            val result = bookRepository.deleteBook(bookId)
            if (result.isSuccess) {
                loadMyShopData()
            }
        }
    }
}
