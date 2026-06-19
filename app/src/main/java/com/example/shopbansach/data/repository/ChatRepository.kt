package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.ChatMessage
import com.example.shopbansach.data.model.ChatRoom
import com.example.shopbansach.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRoomsCollection = firestore.collection("chatRooms")

    /**
     * Lấy hoặc tạo một phòng chat giữa 2 người và đính kèm sản phẩm đang quan tâm
     */
    suspend fun getOrCreateChatRoom(user1: User, user2: User, bookId: String? = null): String {
        val participants = listOf(user1.id, user2.id).sorted()
        val roomId = participants.joinToString("_")

        val doc = chatRoomsCollection.document(roomId).get().await()
        if (!doc.exists()) {
            val chatRoom = ChatRoom(
                id = roomId,
                participantIds = participants,
                participantNames = mapOf(user1.id to user1.name, user2.id to user2.name),
                participantAvatars = mapOf(user1.id to user1.avatarUrl, user2.id to user2.avatarUrl),
                lastBookId = bookId
            )
            chatRoomsCollection.document(roomId).set(chatRoom).await()
        } else if (bookId != null) {
            // Cập nhật sản phẩm quan tâm mới nhất nếu có
            chatRoomsCollection.document(roomId).update("lastBookId", bookId).await()
        }
        return roomId
    }

    suspend fun sendMessage(roomId: String, senderId: String, receiverId: String, message: String): Result<Unit> {
        return try {
            val messageId = firestore.collection("chatRooms").document(roomId)
                .collection("messages").document().id
            
            val chatMessage = ChatMessage(
                id = messageId,
                senderId = senderId,
                receiverId = receiverId,
                message = message,
                timestamp = System.currentTimeMillis()
            )

            val batch = firestore.batch()
            val msgRef = chatRoomsCollection.document(roomId).collection("messages").document(messageId)
            batch.set(msgRef, chatMessage)
            
            // Cập nhật thông tin tin nhắn cuối và tăng số tin chưa đọc cho người nhận
            val chatRoomRef = chatRoomsCollection.document(roomId)
            batch.update(chatRoomRef, mapOf(
                "lastMessage" to message,
                "lastMessageTimestamp" to chatMessage.timestamp,
                "unreadCounts.$receiverId" to com.google.firebase.firestore.FieldValue.increment(1)
            ))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(roomId: String, userId: String) {
        try {
            val batch = firestore.batch()
            
            // 1. Reset unread count cho user này trong ChatRoom
            val chatRoomRef = chatRoomsCollection.document(roomId)
            batch.update(chatRoomRef, "unreadCounts.$userId", 0)
            
            // 2. Đánh dấu các tin nhắn gửi ĐẾN user này là đã đọc
            val unreadMessages = chatRoomRef.collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("read", false)
                .get().await()
                
            for (doc in unreadMessages.documents) {
                batch.update(doc.reference, "read", true)
            }
            
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMessagesFlow(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = chatRoomsCollection.document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(ChatMessage::class.java))
                }
            }
        awaitClose { registration.remove() }
    }

    fun getChatRoomsFlow(userId: String): Flow<List<ChatRoom>> = callbackFlow {
        val registration = chatRoomsCollection
            .whereArrayContains("participantIds", userId)
            // Tạm thời bỏ orderBy để tránh lỗi thiếu Index, 
            // sau khi tạo Index trên Firebase xong bạn có thể thêm lại
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val rooms = snapshot.toObjects(ChatRoom::class.java)
                    // Sắp xếp thủ công bằng code thay vì Query để an toàn
                    trySend(rooms.sortedByDescending { it.lastMessageTimestamp })
                }
            }
        awaitClose { registration.remove() }
    }
}
