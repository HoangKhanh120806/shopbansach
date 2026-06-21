package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Job
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
    private var totalBooksCount: Long = 0
    private var loadJob: Job? = null

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
                totalBooksCount = bookRepository.getTotalBooksCount()
                val totalPages = ceil(totalBooksCount.toDouble() / PAGE_SIZE).toInt().coerceAtLeast(1)
                
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

    fun loadNewArrivals(page: Int, forceRefresh: Boolean = false) {
        if (page < 1 || (page > _uiState.value.totalPages && _uiState.value.totalPages > 0)) return
        
        // Tránh tải lại nếu trang hiện tại đang hiển thị và không phải đang load, TRỪ KHI forceRefresh = true
        if (!forceRefresh && page == _uiState.value.currentPage && _uiState.value.newArrivals.isNotEmpty() && !_uiState.value.isNewArrivalsLoading) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // Ngay lập tức xóa sách cũ và bật trạng thái loading (chỉ xóa nếu force hoặc sang trang mới)
            _uiState.update { it.copy(
                isNewArrivalsLoading = true, 
                currentPage = page,
                newArrivals = if (page != _uiState.value.currentPage || forceRefresh) emptyList() else it.newArrivals
            ) }

            try {
                val result = when {
                    page == 1 -> {
                        bookRepository.getAllBooksPaged(PAGE_SIZE, null)
                    }
                    pageHistory.containsKey(page - 1) -> {
                        bookRepository.getAllBooksPaged(PAGE_SIZE, pageHistory[page - 1])
                    }
                    page == _uiState.value.totalPages -> {
                        val lastPageLimit = if (totalBooksCount % PAGE_SIZE == 0L) PAGE_SIZE else totalBooksCount % PAGE_SIZE
                        bookRepository.getLastPage(lastPageLimit)
                    }
                    else -> {
                        // Nếu nhảy trang mà không có cursor, tải từ đầu
                        bookRepository.getAllBooksPaged(PAGE_SIZE, null)
                    }
                }
                
                // Lưu cursor của trang vừa tải thành công
                pageHistory[page] = result.lastDocument
                
                // Lọc bỏ sách trùng với top 5 Featured
                val featuredIds = _uiState.value.featuredBooks.take(5).map { it.id }.toSet()
                val filteredBooks = result.books
                    .filter { it.id !in featuredIds }
                    .distinctBy { it.id }
                
                _uiState.update { it.copy(
                    newArrivals = filteredBooks,
                    isLastPage = result.isLastPage,
                    isNewArrivalsLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isNewArrivalsLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                // Làm mới cả sách nổi bật và danh sách mới nhất
                val featured = bookRepository.getFeaturedBooks()
                _uiState.update { it.copy(featuredBooks = featured) }
                
                // Ép buộc tải lại dữ liệu mới từ Server
                loadNewArrivals(_uiState.value.currentPage, forceRefresh = true)
            } catch (e: Exception) {
                // Bỏ qua lỗi
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
