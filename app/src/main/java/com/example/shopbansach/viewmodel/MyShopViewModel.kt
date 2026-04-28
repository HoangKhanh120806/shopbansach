package com.example.shopbansach.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyShopUiState(
    val myBooks: List<Book> = emptyList(),
    val currentUser: User? = null,
    val totalRevenue: Long = 0,
    val totalSold: Int = 0,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null
)

class MyShopViewModel(
    private val cloudinaryRepository: CloudinaryRepository,
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyShopUiState())
    val uiState: StateFlow<MyShopUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()

    init {
        loadData()
    }

    fun loadData() {
        loadMyShopBooks()
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    fun loadMyShopBooks() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val books = bookRepository.getBooksByOwner(userId)
                
                // Giả định doanh thu và số lượng bán
                val revenue = books.sumOf { it.price * (it.stock / 2) } 
                val sold = books.sumOf { it.stock / 3 } 
                
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

    fun updateShopName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            val result = authRepository.updateShopName(userId, newName)
            if (result.isSuccess) {
                loadUserData()
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }

    fun updateShopAvatar(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            
            // Upload ảnh lên Cloudinary thay vì Firebase Storage
            val uploadResult = cloudinaryRepository.uploadImage(imageUri, "accound") // Sử dụng preset 'accound' như trong CloudinaryRepository.kt
            
            if (uploadResult.isSuccess) {
                val avatarUrl = uploadResult.getOrNull()
                if (avatarUrl != null) {
                    // Cập nhật shopAvatarUrl vào User Profile trong Firestore
                    val updateResult = authRepository.updateShopAvatarUrl(userId, avatarUrl)
                    if (updateResult.isSuccess) {
                        loadUserData()
                    }
                }
            } else {
                 _uiState.update { it.copy(errorMessage = "Lỗi upload Cloudinary: ${uploadResult.exceptionOrNull()?.message}") }
            }
            _uiState.update { it.copy(isUpdating = false) }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            val result = bookRepository.deleteBook(bookId)
            if (result.isSuccess) {
                loadMyShopBooks()
            }
        }
    }
}
