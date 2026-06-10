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
            emptyList()
        }
    }

    suspend fun addReview(review: Review): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. Thêm review mới
                val reviewDocRef = reviewsCollection.document()
                val reviewWithId = review.copy(id = reviewDocRef.id)
                transaction.set(reviewDocRef, reviewWithId)

                // 2. Lấy tất cả review hiện tại của sách này (bao gồm cả cái vừa thêm - thực tế transaction sẽ chạy tuần tự)
                // Tuy nhiên, transaction.get() không lấy được dữ liệu vừa set trong cùng transaction nếu chưa commit.
                // Vì vậy ta sẽ cập nhật điểm dựa trên dữ liệu cũ + review mới.
                val bookRef = booksCollection.document(review.bookId)
                val bookSnapshot = transaction.get(bookRef)
                
                if (bookSnapshot.exists()) {
                    // Trong thực tế app lớn, ta nên lưu thêm trường reviewCount vào Book model
                    // Ở đây tôi sẽ dùng logic đơn giản: Giả sử ta lấy lại list review để tính toán
                    // Nhưng Firestore Transaction không hỗ trợ Query. Vì vậy ta sẽ thực hiện bước 2 ngoài transaction 
                    // hoặc chấp nhận một giải pháp tối ưu hơn là Cloud Functions.
                    // Với quy mô đồ án, tôi sẽ thực hiện theo cách: Add Review -> Get All Reviews -> Update Book
                }
            }.await()
            
            // Sau khi add thành công, ta thực hiện tính toán lại rating
            updateBookRating(review.bookId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateBookRating(bookId: String) {
        try {
            val allReviews = getReviewsForBook(bookId)
            if (allReviews.isNotEmpty()) {
                val avgRating = allReviews.map { it.rating }.average()
                // Làm tròn 1 chữ số thập phân
                val roundedRating = Math.round(avgRating * 10.0) / 10.0
                booksCollection.document(bookId).update("rating", roundedRating).await()
            }
        } catch (e: Exception) {
            // Log lỗi nếu cần
        }
    }
}
