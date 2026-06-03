package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

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
            val docRef = reviewsCollection.document()
            val reviewWithId = review.copy(id = docRef.id)
            docRef.set(reviewWithId).await()
            
            // Cập nhật lại rating trung bình của sách (Optional - có thể làm sau)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
