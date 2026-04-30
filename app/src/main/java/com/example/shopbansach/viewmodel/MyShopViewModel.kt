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
import java.util.Calendar

data class MyShopUiState(
    val myBooks: List<Book> = emptyList(),
    val bookSoldCounts: Map<String, Int> = emptyMap(),
    val currentUser: User? = null,
    val totalRevenue: Long = 0,
    val deliveredRevenue: Long = 0,
    val pendingRevenue: Long = 0,
    val totalSold: Int = 0,
    val soldToday: Int = 0,
    val newOrdersCount: Int = 0,
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
                // 1. Lấy danh sách sách của tôi trước
                val myBooks = bookRepository.getBooksByOwner(userId)
                val myBookIds = myBooks.map { it.id }.toSet()
                Log.d("MyShopVM", "Found ${myBooks.size} books owned by $userId")

                // 2. Thu thập đơn hàng từ nhiều nguồn để tránh lỗi quyền truy cập/Index
                val allPossibleOrders = mutableListOf<Order>()
                
                // Nguồn A: Đơn hàng chứa sellerId của mình (Cấu trúc mới)
                try {
                    val ordersBySeller = orderRepository.getOrdersBySeller(userId)
                    allPossibleOrders.addAll(ordersBySeller)
                    Log.d("MyShopVM", "Source A (SellerIds): Found ${ordersBySeller.size} orders")
                } catch (e: Exception) {
                    Log.e("MyShopVM", "Source A failed: ${e.message}")
                }

                // Nguồn B: Đơn hàng do chính mình đặt (Nếu mình tự mua hàng của mình để test)
                try {
                    val userOrders = orderRepository.getOrdersByUser(userId)
                    allPossibleOrders.addAll(userOrders)
                    Log.d("MyShopVM", "Source B (UserOrders): Found ${userOrders.size} orders")
                } catch (e: Exception) { }

                // Nguồn C: Thử lấy tất cả đơn hàng (Nếu là Admin hoặc Rules cho phép)
                try {
                    val globalOrders = orderRepository.getAllOrders()
                    allPossibleOrders.addAll(globalOrders)
                    Log.d("MyShopVM", "Source C (AllOrders): Found ${globalOrders.size} orders")
                } catch (e: Exception) { }

                val distinctOrders = allPossibleOrders.distinctBy { it.id }
                Log.d("MyShopVM", "Total unique orders to process: ${distinctOrders.size}")

                var totalRev = 0L
                var deliveredRev = 0L
                var pendingRev = 0L
                var totalSoldCount = 0
                var soldTodayCount = 0
                var newOrdersCount = 0
                val bookSoldMap = mutableMapOf<String, Int>()

                val startOfToday = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // 3. Tính toán doanh thu
                distinctOrders.forEach { order ->
                    val status = order.status.trim().lowercase()
                    if (status != "đã hủy") {
                        var orderContribution = 0L
                        
                        order.items.forEach { item ->
                            // KIỂM TRA: Nếu ownerId khớp HOẶC bookId nằm trong danh sách sách của mình
                            val isMyItem = item.ownerId == userId || myBookIds.contains(item.bookId)
                            
                            if (isMyItem) {
                                val itemTotal = item.price * item.quantity
                                orderContribution += itemTotal
                                totalSoldCount += item.quantity
                                
                                bookSoldMap[item.bookId] = (bookSoldMap[item.bookId] ?: 0) + item.quantity
                                
                                if (order.createdAt >= startOfToday) {
                                    soldTodayCount += item.quantity
                                }
                            }
                        }

                        if (orderContribution > 0) {
                            totalRev += orderContribution
                            when (status) {
                                "đã giao", "thành công" -> deliveredRev += orderContribution
                                "chờ xác nhận" -> {
                                    pendingRev += orderContribution
                                    newOrdersCount++
                                }
                                else -> pendingRev += orderContribution
                            }
                        }
                    }
                }
                
                Log.d("MyShopVM", "Calculation Result: TotalRev=$totalRev, Sold=$totalSoldCount")

                _uiState.update { it.copy(
                    myBooks = myBooks,
                    bookSoldCounts = bookSoldMap,
                    totalRevenue = totalRev,
                    deliveredRevenue = deliveredRev,
                    pendingRevenue = pendingRev,
                    totalSold = totalSoldCount,
                    soldToday = soldTodayCount,
                    newOrdersCount = newOrdersCount,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Log.e("MyShopVM", "Global Load Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateShopName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            if (authRepository.updateShopName(userId, newName).isSuccess) {
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
                if (avatarUrl != null && authRepository.updateShopAvatarUrl(userId, avatarUrl).isSuccess) {
                    loadUserData()
                }
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            if (bookRepository.deleteBook(bookId).isSuccess) {
                loadMyShopData()
            }
        }
    }
}
