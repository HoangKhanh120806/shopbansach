package com.example.shopbansach.data.repository

import android.util.Log
import com.example.shopbansach.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val ordersCollection = firestore.collection("orders")

    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val docRef = ordersCollection.document()
            val orderWithId = order.copy(id = docRef.id)
            docRef.set(orderWithId).await()
            Log.d("OrderRepository", "Order created successfully: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error creating order", e)
            Result.failure(e)
        }
    }

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val document = ordersCollection.document(orderId).get().await()
            val order = document.toObject(Order::class.java)
            if (order == null) Log.e("OrderRepository", "Order not found: $orderId")
            order
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting order by ID", e)
            null
        }
    }

    suspend fun getOrdersByUser(userId: String): List<Order> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting user orders. Falling back to unsorted.")
            try {
                val fallbackSnapshot = ordersCollection.whereEqualTo("userId", userId).get().await()
                fallbackSnapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
            } catch (fallbackEx: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Tối ưu hóa: Chỉ lấy các đơn hàng có chứa sản phẩm của seller này
     */
    suspend fun getOrdersBySeller(sellerId: String): List<Order> {
        return try {
            val snapshot = ordersCollection
                .whereArrayContains("sellerIds", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting seller orders. Falling back to filter.")
            try {
                // Fallback nếu chưa có index cho array-contains + orderBy
                val fallbackSnapshot = ordersCollection.whereArrayContains("sellerIds", sellerId).get().await()
                fallbackSnapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
            } catch (fallbackEx: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getAllOrders(): List<Order> {
        return try {
            val snapshot = ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting all orders. Falling back to unsorted.")
            try {
                val fallbackSnapshot = ordersCollection.get().await()
                fallbackSnapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
            } catch (fallbackEx: Exception) {
                emptyList()
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
