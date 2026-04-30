package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Address
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.CartRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.example.shopbansach.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class CartActionState {
    object Idle : CartActionState()
    object Loading : CartActionState()
    object Success : CartActionState()
    data class Error(val message: String) : CartActionState()
}

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val actionState: CartActionState = CartActionState.Idle,
    val errorMessage: String? = null
)

class CartViewModel(
    private val repository: CartRepository = CartRepository(),
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository(),
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        // Chỉ đăng ký listener, nó sẽ tự động gọi load lần đầu
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                loadCartItems()
            } else {
                _uiState.update { CartUiState() }
            }
        }
    }

    fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rawCartItems = repository.getCartItems()
                if (rawCartItems.isEmpty()) {
                    _uiState.update { it.copy(cartItems = emptyList(), isLoading = false) }
                    return@launch
                }

                val bookIds = rawCartItems.map { it.bookId }
                val allBooks = bookRepository.getBooksByIds(bookIds)

                val validatedItems = rawCartItems.map { item ->
                    val book = allBooks.find { it.id == item.bookId }
                    if (book != null) {
                        item.copy(
                            title = book.title,
                            price = book.price,
                            imageUrl = book.imageUrl,
                            author = book.author,
                            ownerId = book.ownerId,
                            stock = book.stock
                        )
                    } else {
                        item.copy(stock = 0)
                    }
                }

                _uiState.update { it.copy(cartItems = validatedItems, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addToCart(book: Book, quantity: Int = 1, forceSelected: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = CartActionState.Loading) }
            val item = CartItem(
                bookId = book.id,
                title = book.title,
                price = book.price,
                imageUrl = book.imageUrl,
                author = book.author,
                ownerId = book.ownerId,
                quantity = quantity,
                isSelected = forceSelected,
                stock = book.stock
            )
            val result = repository.addToCart(item, forceSelected)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionState = CartActionState.Success) }
                loadCartItems()
            } else {
                _uiState.update { it.copy(actionState = CartActionState.Error(result.exceptionOrNull()?.message ?: "Lỗi thêm vào giỏ")) }
            }
        }
    }

    fun updateQuantity(bookId: String, newQuantity: Int) {
        val currentItems = _uiState.value.cartItems
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map { 
                    if (it.bookId == bookId) it.copy(quantity = if (newQuantity < 1) 1 else newQuantity) else it 
                }
            )
        }
        
        viewModelScope.launch {
            val result = repository.updateQuantity(bookId, newQuantity)
            if (result.isFailure) {
                _uiState.update { it.copy(cartItems = currentItems) }
            }
        }
    }

    fun toggleSelection(bookId: String, isSelected: Boolean) {
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map { 
                    if (it.bookId == bookId) it.copy(isSelected = isSelected) else it 
                }
            )
        }
        viewModelScope.launch { repository.toggleSelection(bookId, isSelected) }
    }

    fun toggleSelectAll(isSelected: Boolean) {
        // Cập nhật UI ngay lập tức
        _uiState.update { state ->
            val newList = state.cartItems.map { 
                if (it.stock > 0) it.copy(isSelected = isSelected) else it.copy(isSelected = false)
            }
            state.copy(cartItems = newList)
        }
        // Cập nhật Database
        viewModelScope.launch { repository.toggleAllSelection(isSelected) }
    }

    fun removeFromCart(bookId: String) {
        viewModelScope.launch {
            if (repository.removeFromCart(bookId).isSuccess) {
                loadCartItems()
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            if (repository.clearCart().isSuccess) {
                _uiState.update { it.copy(cartItems = emptyList()) }
            }
        }
    }

    fun processCheckout(
        checkoutItems: List<CartItem>,
        isBuyNow: Boolean,
        address: Address,
        paymentMethod: String,
        totalPrice: Long,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentUserId = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")

                val orderId = firestore.runTransaction { transaction ->
                    val finalItems = mutableListOf<CartItem>()
                    val sellerIds = mutableSetOf<String>()

                    checkoutItems.forEach { item ->
                        val bookRef = firestore.collection("books").document(item.bookId)
                        val snapshot = transaction.get(bookRef)
                        
                        if (!snapshot.exists()) {
                            throw Exception("Sản phẩm '${item.title}' không còn tồn tại")
                        }

                        val currentStock = snapshot.getLong("stock") ?: 0L
                        if (currentStock < item.quantity) {
                            val title = snapshot.getString("title") ?: item.title
                            throw Exception("Sản phẩm '$title' không đủ tồn kho (Còn lại: $currentStock)")
                        }
                        
                        val ownerId = snapshot.getString("ownerId") ?: ""
                        
                        finalItems.add(item.copy(ownerId = ownerId, stock = currentStock.toInt()))
                        if (ownerId.isNotEmpty()) sellerIds.add(ownerId)

                        transaction.update(bookRef, "stock", currentStock - item.quantity)
                    }

                    val orderRef = firestore.collection("orders").document()
                    val finalOrderId = orderRef.id
                    
                    val newOrder = Order(
                        id = finalOrderId,
                        userId = currentUserId,
                        items = finalItems,
                        totalPrice = totalPrice,
                        shippingAddress = address,
                        paymentMethod = paymentMethod,
                        sellerIds = sellerIds.toList(),
                        createdAt = System.currentTimeMillis()
                    )

                    transaction.set(orderRef, newOrder)

                    if (!isBuyNow) {
                        val cartCollection = firestore.collection("users").document(currentUserId).collection("cart")
                        checkoutItems.forEach { item ->
                            transaction.delete(cartCollection.document(item.bookId))
                        }
                    }

                    finalOrderId
                }.await()

                loadCartItems()
                _uiState.update { it.copy(isLoading = false) }
                onComplete(orderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi thanh toán") }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = CartActionState.Idle, errorMessage = null) }
    }
}
