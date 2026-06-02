package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class ChatMessage(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: String = "",
    
    @get:PropertyName("receiverId") @set:PropertyName("receiverId")
    var receiverId: String = "",
    
    @get:PropertyName("message") @set:PropertyName("message")
    var message: String = "",
    
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)
