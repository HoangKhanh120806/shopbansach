package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellerShopUiState(
    val seller: User? = null,
    val sellerBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SellerShopViewModel(
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SellerShopUiState())
    val uiState: StateFlow<SellerShopUiState> = _uiState.asStateFlow()

    fun loadSellerShop(sellerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val seller = authRepository.getUserById(sellerId)
                val books = bookRepository.getBooksByOwner(sellerId)
                
                _uiState.update { it.copy(
                    seller = seller,
                    sellerBooks = books,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
