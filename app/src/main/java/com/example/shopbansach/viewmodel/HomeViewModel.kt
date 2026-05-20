package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val featuredBooks: List<Book> = emptyList(),
    val newArrivals: List<Book> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val bookRepository = FirebaseBookRepository()
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
        loadData()
        auth.addAuthStateListener(authStateListener)
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val featured = bookRepository.getFeaturedBooks()
                val arrivals = bookRepository.getNewArrivals()
                val user = authRepository.getCurrentUserData()
                
                _uiState.update { it.copy(
                    featuredBooks = featured,
                    newArrivals = arrivals,
                    currentUser = user,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Lỗi: ${e.message}"
                ) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
