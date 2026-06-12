package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class Notification(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "", // ID người nhận
    
    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",
    
    @get:PropertyName("message") @set:PropertyName("message")
    var message: String = "",
    
    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "SYSTEM", // CHAT, ORDER, SYSTEM
    
    @get:PropertyName("orderId") @set:PropertyName("orderId")
    var orderId: String = "",
    
    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,
    
    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)
