package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class ChatRoom(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("participantIds") @set:PropertyName("participantIds")
    var participantIds: List<String> = emptyList(),
    
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage")
    var lastMessage: String = "",
    
    @get:PropertyName("lastMessageTimestamp") @set:PropertyName("lastMessageTimestamp")
    var lastMessageTimestamp: Long = System.currentTimeMillis(),
    
    // Lưu tên và ảnh để hiển thị danh sách nhanh hơn
    @get:PropertyName("participantNames") @set:PropertyName("participantNames")
    var participantNames: Map<String, String> = emptyMap(),
    
    @get:PropertyName("participantAvatars") @set:PropertyName("participantAvatars")
    var participantAvatars: Map<String, String?> = emptyMap()
)
