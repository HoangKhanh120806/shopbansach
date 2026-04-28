package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyShopUiState(
    val myBooks: List<Book> = emptyList(),
    val totalRevenue: Long = 0,
    val totalSold: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyShopViewModel(private val repository: FirebaseBookRepository = FirebaseBookRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(MyShopUiState())
    val uiState: StateFlow<MyShopUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()

    init {
        loadMyShopBooks()
    }

    fun loadMyShopBooks() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val books = repository.getBooksByOwner(userId)
                
                // Giả định doanh thu và số lượng bán (Trong thực tế cần bảng Orders)
                // Ở đây ta tạm tính tổng giá trị hàng đang bán hoặc giả lập số liệu
                val revenue = books.sumOf { it.price * (it.stock / 2) } // Giả lập: bán được một nửa
                val sold = books.sumOf { it.stock / 3 } // Giả lập: bán được 1/3
                
                _uiState.update { it.copy(
                    myBooks = books, 
                    totalRevenue = revenue,
                    totalSold = sold,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            val result = repository.deleteBook(bookId)
            if (result.isSuccess) {
                loadMyShopBooks()
            }
        }
    }
}
