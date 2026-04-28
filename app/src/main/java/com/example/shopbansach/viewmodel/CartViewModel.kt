package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.data.repository.CartRepository
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

class CartViewModel(private val repository: CartRepository = CartRepository()) : ViewModel() {

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
        // Cập nhật cục bộ trước (Optimistic UI)
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
                loadCartItems() // Rollback nếu lỗi
            }
        }
    }

    fun toggleSelection(bookId: String, isSelected: Boolean) {
        // Cập nhật cục bộ ngay lập tức để UI thay đổi ngay
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
                // Nếu lưu database thất bại, tải lại để đồng bộ đúng dữ liệu thực tế
                loadCartItems()
            }
        }
    }

    fun toggleSelectAll(isSelected: Boolean) {
        // Cập nhật cục bộ cho tất cả
        _uiState.update { state ->
            state.copy(
                cartItems = state.cartItems.map { it.copy(isSelected = isSelected) }
            )
        }

        viewModelScope.launch {
            // Trong thực tế nên dùng batch update ở repository
            _uiState.value.cartItems.forEach { 
                repository.toggleSelection(it.bookId, isSelected)
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

    fun resetActionState() {
        _uiState.update { it.copy(actionState = CartActionState.Idle) }
    }
}
