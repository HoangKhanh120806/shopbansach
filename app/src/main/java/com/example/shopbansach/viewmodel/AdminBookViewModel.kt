package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBookUiState(
    val books: List<Book> = emptyList(),
    val filteredBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLastPage: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class AdminBookViewModel(private val repository: FirebaseBookRepository = FirebaseBookRepository()) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminBookUiState())
    val uiState: StateFlow<AdminBookUiState> = _uiState.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private val PAGE_SIZE = 12L

    init {
        refreshBooks()
    }

    fun refreshBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, isLastPage = false) }
            lastDocument = null
            try {
                val page = repository.getAllBooksPaged(PAGE_SIZE, null)
                lastDocument = page.lastDocument
                _uiState.update { it.copy(
                    books = page.books, 
                    filteredBooks = filterList(page.books, it.searchQuery),
                    isRefreshing = false,
                    isLastPage = page.isLastPage
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, errorMessage = "Lỗi tải sách: ${e.message}") }
            }
        }
    }

    fun loadMoreBooks() {
        val currentState = _uiState.value
        // Không tải thêm nếu đang tải, đã hết trang hoặc đang trong chế độ tìm kiếm
        if (currentState.isLoadingMore || currentState.isLastPage || currentState.searchQuery.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val page = repository.getAllBooksPaged(PAGE_SIZE, lastDocument)
                lastDocument = page.lastDocument
                
                val newBooksList = currentState.books + page.books
                _uiState.update { it.copy(
                    books = newBooksList,
                    filteredBooks = filterList(newBooksList, it.searchQuery),
                    isLoadingMore = false,
                    isLastPage = page.isLastPage
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMore = false, errorMessage = "Không thể tải thêm: ${e.message}") }
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
                _uiState.update { it.copy(actionSuccessMessage = "Đã xóa sách thành công", isLoading = false) }
                refreshBooks()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi khi xóa sách: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
