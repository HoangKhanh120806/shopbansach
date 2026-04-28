package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
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
            
            // Kiểm tra xem sách đã có trong giỏ hàng chưa
            val docRef = collection.document(cartItem.bookId)
            val snapshot = docRef.get().await()
            
            if (snapshot.exists()) {
                // Nếu có rồi thì tăng số lượng
                val currentQty = snapshot.getLong("quantity") ?: 1
                docRef.update("quantity", currentQty + cartItem.quantity).await()
            } else {
                // Nếu chưa có thì thêm mới
                docRef.set(cartItem).await()
            }
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
