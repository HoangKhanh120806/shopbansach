package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.ChatMessage
import com.example.shopbansach.data.model.ChatRoom
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val chatRooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = false,
    val currentRoomId: String? = null,
    val otherUser: User? = null
)

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()

    fun loadChatRooms() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getChatRoomsFlow(userId).collect { rooms ->
                _uiState.update { it.copy(chatRooms = rooms, isLoading = false) }
            }
        }
    }

    fun startChatWithUser(sellerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUserData = authRepository.getCurrentUserData()
            val sellerData = authRepository.getUserById(sellerId)
            
            if (currentUserData != null && sellerData != null) {
                val roomId = repository.getOrCreateChatRoom(currentUserData, sellerData)
                _uiState.update { it.copy(currentRoomId = roomId, otherUser = sellerData) }
                observeMessages(roomId)
            }
        }
    }

    fun observeMessages(roomId: String) {
        viewModelScope.launch {
            repository.getMessagesFlow(roomId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs, isLoading = false) }
            }
        }
    }

    fun sendMessage(message: String) {
        val roomId = _uiState.value.currentRoomId ?: return
        val senderId = auth.currentUser?.uid ?: return
        val receiverId = _uiState.value.otherUser?.id ?: return
        
        if (message.isBlank()) return
        
        viewModelScope.launch {
            repository.sendMessage(roomId, senderId, receiverId, message)
        }
    }
}
