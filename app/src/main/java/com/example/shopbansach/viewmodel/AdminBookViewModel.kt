package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBookUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminBookViewModel(private val repository: FirebaseBookRepository = FirebaseBookRepository()) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminBookUiState())
    val uiState: StateFlow<AdminBookUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Lấy tất cả sách (có thể tăng giới hạn cho Admin)
                val bookList = repository.getAllBooks(limit = 100)
                _uiState.update { it.copy(books = bookList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            val result = repository.deleteBook(bookId)
            if (result.isSuccess) {
                loadBooks() // Refresh list
            }
        }
    }
}
