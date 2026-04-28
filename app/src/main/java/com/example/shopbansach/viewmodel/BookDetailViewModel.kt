package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BookDetailViewModel(private val repository: FirebaseBookRepository = FirebaseBookRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun getBookDetail(bookId: String) {
        viewModelScope.launch {
            _uiState.value = BookDetailUiState(isLoading = true)
            try {
                val book = repository.getBookById(bookId)
                if (book != null) {
                    _uiState.value = BookDetailUiState(book = book, isLoading = false)
                } else {
                    _uiState.value = BookDetailUiState(isLoading = false, errorMessage = "Không tìm thấy sách")
                }
            } catch (e: Exception) {
                _uiState.value = BookDetailUiState(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
