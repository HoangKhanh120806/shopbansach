package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCartCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("cart")
    }

    suspend fun addToCart(cartItem: CartItem): Result<Unit> {
        return try {
            val collection = getCartCollection() ?: throw Exception("User not logged in")
            val docRef = collection.document(cartItem.bookId)
            
            // Sử dụng transaction hoặc kiểm tra existence để đảm bảo tính nguyên tử
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    // Nếu đã có, chỉ tăng số lượng
                    transaction.update(docRef, "quantity", FieldValue.increment(cartItem.quantity.toLong()))
                } else {
                    // Nếu chưa có, thêm mới cả object
                    transaction.set(docRef, cartItem)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCartItems(): List<CartItem> {
        return try {
            val collection = getCartCollection() ?: return emptyList()
            val snapshot = collection.get().await()
            snapshot.toObjects(CartItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateQuantity(bookId: String, newQuantity: Int): Result<Unit> {
        return try {
            val collection = getCartCollection() ?: throw Exception("User not logged in")
            if (newQuantity <= 0) {
                collection.document(bookId).delete().await()
            } else {
                collection.document(bookId).update("quantity", newQuantity).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(bookId: String): Result<Unit> {
        return try {
            val collection = getCartCollection() ?: throw Exception("User not logged in")
            collection.document(bookId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearCart(): Result<Unit> {
        return try {
            val collection = getCartCollection() ?: throw Exception("User not logged in")
            val snapshot = collection.get().await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
