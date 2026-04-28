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
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

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

    fun saveBook(
        bookId: String? = null,
        title: String,
        author: String,
        price: String,
        pages: String,
        synopsis: String,
        category: String,
        stock: String,
        imageUri: Uri?,
        existingImageUrl: String? = null,
        existingPublicId: String? = null,
        rating: Double = 0.0
    ) {
        val cleanPrice = price.replace(Regex("[^0-9]"), "")
        val cleanPages = pages.replace(Regex("[^0-9]"), "")
        val cleanStock = stock.replace(Regex("[^0-9]"), "")

        if (title.isEmpty() || cleanPrice.isEmpty() || (imageUri == null && existingImageUrl == null)) {
            _actionState.value = BookActionState.Error("Vui lòng nhập đầy đủ thông tin (Tên, Giá) và chọn ảnh")
            return
        }

        viewModelScope.launch {
            _actionState.value = BookActionState.Loading
            try {
                var finalImageUrl = existingImageUrl
                var finalPublicId = existingPublicId

                if (imageUri != null) {
                    val uploadResult = cloudinaryRepository.uploadImage(imageUri)
                    if (uploadResult.isSuccess) {
                        val (url, publicId) = uploadResult.getOrThrow()
                        
                        // Nếu đang sửa sách và có ảnh cũ, có thể xóa ảnh cũ trên Cloudinary ở đây
                        if (!existingPublicId.isNullOrEmpty()) {
                            cloudinaryRepository.deleteImage(existingPublicId)
                        }
                        
                        finalImageUrl = url
                        finalPublicId = publicId
                    } else {
                        _actionState.value = BookActionState.Error("Lỗi upload ảnh")
                        return@launch
                    }
                }

                val id = bookId ?: UUID.randomUUID().toString()
                val newBook = Book(
                    id = id,
                    title = title,
                    titleLowercase = title.lowercase(Locale.ROOT),
                    author = author,
                    price = cleanPrice.toLongOrNull() ?: 0L,
                    pages = cleanPages.toIntOrNull() ?: 0,
                    synopsis = synopsis,
                    imageUrl = finalImageUrl,
                    imagePublicId = finalPublicId,
                    ownerId = auth.currentUser?.uid ?: "",
                    category = category.ifEmpty { "Khác" },
                    stock = cleanStock.toIntOrNull() ?: 0,
                    rating = rating
                )
                
                val result = bookRepository.addBook(newBook)
                if (result.isSuccess) {
                    _actionState.value = BookActionState.Success
                } else {
                    _actionState.value = BookActionState.Error("Lỗi lưu dữ liệu: ${result.exceptionOrNull()?.message}")
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
