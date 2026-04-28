package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.data.repository.CartRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
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
    val isLoading: Boolean = false,
    val actionState: CartActionState = CartActionState.Idle,
    val errorMessage: String? = null
)

class CartViewModel(
    private val repository: CartRepository = CartRepository(),
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository()
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
                val items = repository.getCartItems()
                _uiState.update { it.copy(cartItems = items, isLoading = false) }
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
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map { 
                    if (it.bookId == bookId) it.copy(quantity = if (newQuantity < 1) 1 else newQuantity) else it 
                }
            )
        }
        
        viewModelScope.launch {
            val result = repository.updateQuantity(bookId, newQuantity)
            if (!result.isSuccess) {
                loadCartItems()
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
            val result = repository.toggleSelection(bookId, isSelected)
            if (!result.isSuccess) {
                loadCartItems()
            }
        }
    }

    fun toggleSelectAll(isSelected: Boolean) {
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map { it.copy(isSelected = isSelected) }
            )
        }

        viewModelScope.launch {
            val result = repository.toggleAllSelection(isSelected)
            if (!result.isSuccess) {
                loadCartItems()
            }
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

    fun clearCart(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.clearCart()
            if (result.isSuccess) {
                loadCartItems()
                onComplete()
            }
        }
    }

    fun clearSelectedItems(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.clearSelectedItems()
            if (result.isSuccess) {
                loadCartItems()
                onComplete()
            }
        }
    }

    /**
     * Xử lý thanh toán an toàn: Kiểm tra tồn kho trước khi trừ
     */
    fun processCheckout(checkoutItems: List<CartItem>, isBuyNow: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Duyệt qua từng sản phẩm để cập nhật tồn kho bằng Transaction
                for (item in checkoutItems) {
                    val result = bookRepository.updateStockWithCheck(item.bookId, item.quantity)
                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("Lỗi cập nhật tồn kho cho sản phẩm ${item.title}")
                    }
                }

                if (!isBuyNow) {
                    repository.clearSelectedItems()
                    loadCartItems()
                }

                _uiState.update { it.copy(isLoading = false) }
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = CartActionState.Idle, errorMessage = null) }
    }
}
