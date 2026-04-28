package com.example.shopbansach.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BookActionState {
    object Idle : BookActionState()
    object Loading : BookActionState()
    object Success : BookActionState()
    data class Error(val message: String) : BookActionState()
}

class BookViewModel(
    private val cloudinaryRepository: CloudinaryRepository,
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository()
) : ViewModel() {

    private val _actionState = MutableStateFlow<BookActionState>(BookActionState.Idle)
    val actionState: StateFlow<BookActionState> = _actionState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun addBook(
        title: String,
        author: String,
        price: String,
        pages: String,
        synopsis: String,
        category: String,
        stock: String,
        imageUri: Uri?
    ) {
        if (title.isEmpty() || price.isEmpty() || imageUri == null) {
            _actionState.value = BookActionState.Error("Vui lòng nhập đầy đủ thông tin bắt buộc và chọn ảnh")
            return
        }

        viewModelScope.launch {
            _actionState.value = BookActionState.Loading
            try {
                // 1. Upload ảnh lên Cloudinary
                val uploadResult = cloudinaryRepository.uploadImage(imageUri)
                if (uploadResult.isSuccess) {
                    val imageUrl = uploadResult.getOrNull()
                    
                    // 2. Lưu vào Firestore
                    val bookId = java.util.UUID.randomUUID().toString()
                    val newBook = Book(
                        id = bookId,
                        title = title,
                        author = author,
                        price = "${price}đ",
                        pages = pages.toIntOrNull() ?: 0,
                        synopsis = synopsis,
                        imageUrl = imageUrl,
                        ownerId = auth.currentUser?.uid ?: "",
                        category = category.ifEmpty { "Khác" },
                        stock = stock.toIntOrNull() ?: 0,
                        rating = 0.0 // Mặc định 0 sao cho sách mới
                    )
                    
                    bookRepository.addBook(newBook)
                    _actionState.value = BookActionState.Success
                } else {
                    _actionState.value = BookActionState.Error("Lỗi upload ảnh")
                }
            } catch (e: Exception) {
                _actionState.value = BookActionState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun resetState() {
        _actionState.value = BookActionState.Idle
    }
}
