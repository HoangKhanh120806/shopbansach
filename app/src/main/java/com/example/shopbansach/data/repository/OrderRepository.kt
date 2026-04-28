package com.example.shopbansach.data.repository

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
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
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
