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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CartActionState {
    object Idle : CartActionState()
    object Loading : CartActionState()
    object Success : CartActionState()
    data class Error(val message: String) : CartActionState()
}

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = true, // Mặc định là true để tránh flash màn hình trống
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
                            author = book.author
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
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("Chưa đăng nhập")

                for (item in checkoutItems) {
                    val result = bookRepository.updateStockWithCheck(item.bookId, item.quantity)
                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("Sản phẩm ${item.title} không đủ tồn kho")
                    }
                }

                val newOrder = Order(
                    userId = currentUserId,
                    items = checkoutItems,
                    totalPrice = totalPrice,
                    shippingAddress = address,
                    paymentMethod = paymentMethod
                )
                
                val orderResult = orderRepository.createOrder(newOrder)
                val orderId = orderResult.getOrThrow()

                val purchasedBookIds = checkoutItems.map { it.bookId }
                repository.removeItemsFromCart(purchasedBookIds)
                
                loadCartItems()

                _uiState.update { it.copy(isLoading = false) }
                onComplete(orderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = CartActionState.Idle, errorMessage = null) }
    }
}
