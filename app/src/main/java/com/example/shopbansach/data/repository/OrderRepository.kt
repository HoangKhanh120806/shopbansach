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
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            // Fallback nếu chưa có index cho orderBy
            val snapshot = ordersCollection.whereEqualTo("userId", userId).get().await()
            snapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
        }
    }
    
    /**
     * Kiểm tra xem user đã từng mua cuốn sách này chưa
     */
    suspend fun hasUserPurchasedBook(userId: String, bookId: String): Boolean {
        return try {
            // Lấy tất cả đơn hàng của user này
            val orders = getOrdersByUser(userId)
            // Kiểm tra xem có đơn hàng nào chứa bookId này không
            // Thông thường nên check status là "Hoàn thành" hoặc "Đã giao hàng"
            orders.any { order -> 
                order.items.any { it.bookId == bookId } 
                // && (order.status == "Hoàn thành" || order.status == "Đã giao hàng") 
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lấy đơn hàng cho Seller - Tối ưu hóa truy vấn
     */
    suspend fun getOrdersBySeller(sellerId: String): List<Order> {
        return try {
            // Sử dụng whereArrayContains là cách tối ưu nhất khi sellerIds đã được lưu đúng
            val snapshot = ordersCollection
                .whereArrayContains("sellerIds", sellerId)
                .get().await()
            
            val orders = snapshot.toObjects(Order::class.java)
            
            // Nếu không tìm thấy đơn nào bằng sellerIds (có thể do đơn cũ), 
            // ta mới dùng fallback quét nhẹ nhàng thay vì quét toàn bộ DB
            if (orders.isEmpty()) {
                Log.d("OrderRepo", "No orders found via sellerIds for $sellerId, trying fallback...")
                val recentSnapshot = ordersCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(100)
                    .get().await()
                
                recentSnapshot.toObjects(Order::class.java).filter { order ->
                    order.items.any { it.ownerId == sellerId }
                }
            } else {
                orders.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            Log.e("OrderRepo", "Error getting orders for seller: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllOrders(): List<Order> {
        return try {
            val snapshot = ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            val snapshot = ordersCollection.get().await()
            snapshot.toObjects(Order::class.java).sortedByDescending { it.createdAt }
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
