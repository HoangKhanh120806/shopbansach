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

data class BookDetailUiState(
    val book: Book? = null,
    val seller: User? = null,
    val relatedBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BookDetailViewModel(
    private val repository: FirebaseBookRepository = FirebaseBookRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun getBookDetail(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val book = repository.getBookById(bookId)
                if (book != null) {
                    // Lấy thông tin người bán (Shop)
                    val seller = authRepository.getUserById(book.ownerId)
                    
                    // Lấy sách liên quan
                    val allBooks = repository.getAllBooks(limit = 20)
                    val related = allBooks.filter { 
                        it.id != bookId && (it.category == book.category || it.author == book.author)
                    }.take(6)
                    
                    _uiState.update { it.copy(
                        book = book, 
                        seller = seller,
                        relatedBooks = related, 
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy sách") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
