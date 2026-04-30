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
            Log.d("OrderRepo", "Order created: ${docRef.id} with sellerIds: ${order.sellerIds}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("OrderRepo", "Error creating order", e)
            Result.failure(e)
        }
    }

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val document = ordersCollection.document(orderId).get().await()
            document.toObject(Order::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrdersByUser(userId: String): List<Order> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .get().await()
            snapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Lấy đơn hàng cho Seller - Phối hợp nhiều nguồn tìm kiếm
     */
    suspend fun getOrdersBySeller(sellerId: String): List<Order> {
        val resultOrders = mutableSetOf<Order>()
        
        // Nguồn 1: Tìm qua sellerIds (Cách chuẩn cho đơn mới)
        try {
            val snapshot = ordersCollection
                .whereArrayContains("sellerIds", sellerId)
                .get().await()
            val orders = snapshot.toObjects(Order::class.java)
            resultOrders.addAll(orders)
            Log.d("OrderRepo", "Source 1 (sellerIds) found: ${orders.size}")
        } catch (e: Exception) {
            Log.e("OrderRepo", "Source 1 failed: ${e.message}")
        }

        // Nguồn 2: Tìm qua userId (Nếu shop tự mua hàng của mình để test)
        try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", sellerId)
                .get().await()
            val orders = snapshot.toObjects(Order::class.java)
            // Lọc lại: chỉ lấy đơn nào thực sự có sách của shop này
            val relevantOrders = orders.filter { order ->
                order.items.any { it.ownerId == sellerId }
            }
            resultOrders.addAll(relevantOrders)
            Log.d("OrderRepo", "Source 2 (userSelf) found: ${relevantOrders.size}")
        } catch (e: Exception) { }

        // Nguồn 3: Quét toàn bộ (Nếu là Admin hoặc đơn hàng cũ chưa có sellerIds)
        if (resultOrders.isEmpty()) {
            try {
                val snapshot = ordersCollection.limit(100).get().await()
                val allOrders = snapshot.toObjects(Order::class.java)
                val filtered = allOrders.filter { order ->
                    order.items.any { it.ownerId == sellerId } || order.sellerIds.contains(sellerId)
                }
                resultOrders.addAll(filtered)
                Log.d("OrderRepo", "Source 3 (Scan) found: ${filtered.size}")
            } catch (e: Exception) {
                Log.e("OrderRepo", "Source 3 failed (Permissions?): ${e.message}")
            }
        }

        return resultOrders.toList().sortedByDescending { it.createdAt }
    }

    suspend fun getAllOrders(): List<Order> {
        return try {
            val snapshot = ordersCollection.get().await()
            snapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
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
