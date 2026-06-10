package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.example.shopbansach.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishlistUiState(
    val wishlistedBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository = WishlistRepository(),
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    fun loadWishlist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bookIds = wishlistRepository.getWishlistedBookIds()
                if (bookIds.isEmpty()) {
                    _uiState.update { it.copy(wishlistedBooks = emptyList(), isLoading = false) }
                    return@launch
                }
                
                val books = bookRepository.getBooksByIds(bookIds)
                _uiState.update { it.copy(wishlistedBooks = books, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun removeFromWishlist(bookId: String) {
        viewModelScope.launch {
            val result = wishlistRepository.toggleWishlist(bookId)
            if (result.isSuccess && !result.getOrDefault(true)) {
                // Nếu xóa thành công (kết quả là false), cập nhật list tại chỗ
                _uiState.update { state ->
                    state.copy(wishlistedBooks = state.wishlistedBooks.filter { it.id != bookId })
                }
            }
        }
    }
}
