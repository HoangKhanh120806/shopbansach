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
        loadCartItems()
    }

    fun loadCartItems() {
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
                            ownerId = book.ownerId // Cập nhật ownerId từ Book
                        )
                    } else {
                        item
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
                ownerId = book.ownerId, // Thêm ownerId khi thêm vào giỏ
                quantity = quantity,
                isSelected = forceSelected
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

        viewModelScope.launch {
            repository.toggleSelection(bookId, isSelected)
        }
    }

    fun toggleSelectAll(isSelected: Boolean) {
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map { it.copy(isSelected = isSelected) }
            )
        }

        viewModelScope.launch {
            repository.toggleAllSelection(isSelected)
        }
    }

    fun removeFromCart(bookId: String) {
        viewModelScope.launch {
            val result = repository.removeFromCart(bookId)
            if (result.isSuccess) {
                loadCartItems()
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val result = repository.clearCart()
            if (result.isSuccess) {
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
                    val bookRefs = checkoutItems.map { item ->
                        val ref = firestore.collection("books").document(item.bookId)
                        val snapshot = transaction.get(ref)
                        
                        val currentStock = snapshot.getLong("stock") ?: 0L
                        if (currentStock < item.quantity) {
                            throw Exception("Sản phẩm '${snapshot.getString("title")}' không đủ tồn kho")
                        }
                        
                        ref to (currentStock - item.quantity)
                    }

                    val orderRef = firestore.collection("orders").document()
                    val finalOrderId = orderRef.id
                    
                    val newOrder = Order(
                        id = finalOrderId,
                        userId = currentUserId,
                        items = checkoutItems,
                        totalPrice = totalPrice,
                        shippingAddress = address,
                        paymentMethod = paymentMethod,
                        createdAt = System.currentTimeMillis()
                    )

                    bookRefs.forEach { (ref, newStock) ->
                        transaction.update(ref, "stock", newStock)
                    }
                    
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
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Đã xảy ra lỗi khi đặt hàng") }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = CartActionState.Idle, errorMessage = null) }
    }
}
