package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val booksCollection = firestore.collection("books")

    suspend fun getReviewsForBook(bookId: String): List<Review> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("bookId", bookId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Review::class.java)
        } catch (e: Exception) {
            // Fallback: Nếu lỗi do thiếu Index (thường gặp ở Firebase), lấy không sắp xếp rồi tự sort bằng code
            try {
                val fallbackSnapshot = reviewsCollection
                    .whereEqualTo("bookId", bookId)
                    .get()
                    .await()
                fallbackSnapshot.toObjects(Review::class.java).sortedByDescending { it.createdAt }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun addReview(review: Review): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. READ: Tất cả các lệnh đọc (get) phải thực hiện trước
                val bookRef = booksCollection.document(review.bookId)
                val bookSnapshot = transaction.get(bookRef)

                // 2. WRITE: Sau đó mới đến các lệnh ghi (set, update, delete)
                val reviewDocRef = reviewsCollection.document()
                val reviewWithId = review.copy(id = reviewDocRef.id)
                transaction.set(reviewDocRef, reviewWithId)
                
                // Cập nhật rating và reviewCount trực tiếp trong transaction để đảm bảo chính xác tuyệt đối
                if (bookSnapshot.exists()) {
                    val currentRating = bookSnapshot.getDouble("rating") ?: 0.0
                    val currentCount = bookSnapshot.getLong("reviewCount") ?: 0L
                    
                    val newCount = currentCount + 1
                    val newRating = (currentRating * currentCount + review.rating) / newCount
                    // Làm tròn 1 chữ số thập phân
                    val roundedRating = Math.round(newRating * 10.0) / 10.0
                    
                    transaction.update(bookRef, 
                        "rating", roundedRating,
                        "reviewCount", newCount.toInt()
                    )
                }
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserReviewed(userId: String, bookId: String): Boolean {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("bookId", bookId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
