package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.Review
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.example.shopbansach.data.repository.OrderRepository
import com.example.shopbansach.data.repository.ReviewRepository
import com.example.shopbansach.data.repository.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val book: Book? = null,
    val seller: User? = null,
    val relatedBooks: List<Book> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val isReviewLoading: Boolean = false,
    val canReview: Boolean = false,
    val reviewMessage: String? = null,
    val isWishlisted: Boolean = false,
    val errorMessage: String? = null
)

class BookDetailViewModel(
    private val repository: FirebaseBookRepository = FirebaseBookRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val reviewRepository: ReviewRepository = ReviewRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val wishlistRepository: WishlistRepository = WishlistRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()
    private val auth = FirebaseAuth.getInstance()

    fun getBookDetail(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val book = repository.getBookById(bookId)
                if (book != null) {
                    val seller = authRepository.getUserById(book.ownerId)
                    val allBooks = repository.getAllBooks(limit = 20)
                    val related = allBooks.filter { 
                        it.id != bookId && (it.category == book.category || it.author == book.author)
                    }.take(6)
                    
                    val reviews = reviewRepository.getReviewsForBook(bookId)
                    
                    val currentUserId = auth.currentUser?.uid
                    val hasPurchased = if (currentUserId != null) {
                        orderRepository.hasUserPurchasedBook(currentUserId, bookId)
                    } else {
                        false
                    }
                    val alreadyReviewed = if (currentUserId != null) {
                        reviewRepository.hasUserReviewed(currentUserId, bookId)
                    } else {
                        false
                    }
                    
                    val canReview = hasPurchased && !alreadyReviewed
                    val reviewMessage = when {
                        currentUserId == null -> "Đăng nhập để đánh giá"
                        !hasPurchased -> "Bạn chỉ có thể đánh giá sau khi đã mua sách này"
                        alreadyReviewed -> "Bạn đã đánh giá sản phẩm này rồi"
                        else -> null
                    }

                    val isWishlisted = if (currentUserId != null) {
                        wishlistRepository.isBookWishlisted(bookId)
                    } else {
                        false
                    }
                    
                    _uiState.update { it.copy(
                        book = book, 
                        seller = seller,
                        relatedBooks = related,
                        reviews = reviews,
                        canReview = canReview,
                        reviewMessage = reviewMessage,
                        isWishlisted = isWishlisted,
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

    fun toggleWishlist(bookId: String) {
        viewModelScope.launch {
            val result = wishlistRepository.toggleWishlist(bookId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isWishlisted = result.getOrDefault(false)) }
            }
        }
    }

    fun submitReview(bookId: String, rating: Int, comment: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewLoading = true) }
            try {
                val user = authRepository.getCurrentUserData() ?: throw Exception("Bạn cần đăng nhập để đánh giá")
                
                val hasPurchased = orderRepository.hasUserPurchasedBook(user.id, bookId)
                if (!hasPurchased) throw Exception("Bạn chỉ có thể đánh giá sau khi đã mua sách này")
                
                val alreadyReviewed = reviewRepository.hasUserReviewed(user.id, bookId)
                if (alreadyReviewed) throw Exception("Bạn đã đánh giá sản phẩm này rồi")

                val review = Review(
                    bookId = bookId,
                    userId = user.id,
                    userName = user.name,
                    userAvatarUrl = user.avatarUrl,
                    rating = rating,
                    comment = comment
                )
                val result = reviewRepository.addReview(review)
                if (result.isSuccess) {
                    // Đợi 1 chút để Firestore cập nhật xong (vì transaction commit xong nhưng SERVER có thể trễ vài ms)
                    // Tuy nhiên getBookById với ID thường lấy bản copy mới nhất từ Server.
                    val updatedBook = repository.getBookById(bookId)
                    val newReviews = reviewRepository.getReviewsForBook(bookId)
                    
                    _uiState.update { it.copy(
                        book = updatedBook,
                        reviews = newReviews, 
                        canReview = false, // Vừa đánh giá xong thì không đánh giá tiếp được
                        reviewMessage = "Bạn đã đánh giá sản phẩm này rồi",
                        isReviewLoading = false
                    ) }
                    onComplete(Result.success(Unit))
                } else {
                    _uiState.update { it.copy(isReviewLoading = false) }
                    onComplete(Result.failure(result.exceptionOrNull() ?: Exception("Lỗi không xác định")))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isReviewLoading = false) }
                onComplete(Result.failure(e))
            }
        }
    }
}
