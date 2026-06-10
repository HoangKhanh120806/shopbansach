package com.example.shopbansach.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WishlistRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getWishlistCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("wishlist")
    }

    suspend fun toggleWishlist(bookId: String): Result<Boolean> {
        return try {
            val collection = getWishlistCollection() ?: throw Exception("Chưa đăng nhập")
            val docRef = collection.document(bookId)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                docRef.delete().await()
                Result.success(false) // Đã xóa khỏi yêu thích
            } else {
                docRef.set(mapOf("bookId" to bookId, "addedAt" to System.currentTimeMillis())).await()
                Result.success(true) // Đã thêm vào yêu thích
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isBookWishlisted(bookId: String): Boolean {
        return try {
            val collection = getWishlistCollection() ?: return false
            collection.document(bookId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getWishlistedBookIds(): List<String> {
        return try {
            val collection = getWishlistCollection() ?: return emptyList()
            val snapshot = collection.orderBy("addedAt").get().await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
