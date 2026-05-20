package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil

data class HomeUiState(
    val featuredBooks: List<Book> = emptyList(),
    val newArrivals: List<Book> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val isNewArrivalsLoading: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isLastPage: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val bookRepository = FirebaseBookRepository()
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val pageHistory = mutableMapOf<Int, DocumentSnapshot?>()
    private val PAGE_SIZE = 10L

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        viewModelScope.launch {
            if (firebaseAuth.currentUser != null) {
                val user = authRepository.getCurrentUserData()
                _uiState.update { it.copy(currentUser = user) }
            } else {
                _uiState.update { it.copy(currentUser = null) }
            }
        }
    }

    init {
        loadInitialData()
        auth.addAuthStateListener(authStateListener)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val featured = bookRepository.getFeaturedBooks()
                val totalBooks = bookRepository.getTotalBooksCount()
                val totalPages = ceil(totalBooks.toDouble() / PAGE_SIZE).toInt().coerceAtLeast(1)
                
                _uiState.update { it.copy(
                    featuredBooks = featured, 
                    totalPages = totalPages,
                    isLoading = false 
                ) }
                loadNewArrivals(1)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadNewArrivals(page: Int) {
        if (page < 1 || (page > _uiState.value.totalPages && _uiState.value.totalPages > 0)) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isNewArrivalsLoading = true, currentPage = page) }
            try {
                val result = if (page == _uiState.value.totalPages && page > 1) {
                    // Logic lấy trang cuối đặc biệt
                    bookRepository.getLastPage(PAGE_SIZE)
                } else {
                    val lastDoc = if (page > 1) pageHistory[page - 1] else null
                    bookRepository.getAllBooksPaged(PAGE_SIZE, lastDoc)
                }
                
                pageHistory[page] = result.lastDocument
                
                _uiState.update { it.copy(
                    newArrivals = result.books,
                    isLastPage = result.isLastPage,
                    isNewArrivalsLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isNewArrivalsLoading = false, errorMessage = e.message) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
