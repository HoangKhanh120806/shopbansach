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
    val filteredBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
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
                // Lấy tất cả sách cho Admin (giới hạn cao hơn)
                val bookList = repository.getAllBooks(limit = 200)
                _uiState.update { it.copy(
                    books = bookList, 
                    filteredBooks = filterList(bookList, _uiState.value.searchQuery),
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi tải sách: ${e.message}") }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredBooks = filterList(it.books, query)
            ) 
        }
    }

    private fun filterList(books: List<Book>, query: String): List<Book> {
        if (query.isEmpty()) return books
        return books.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.author.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteBook(bookId)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccessMessage = "Đã xóa sách thành công") }
                loadBooks()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi khi xóa sách: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
