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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val chatRooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = false,
    val currentRoomId: String? = null,
    val otherUser: User? = null,
    val errorMessage: String? = null
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
            repository.getChatRoomsFlow(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { rooms ->
                    _uiState.update { it.copy(chatRooms = rooms, isLoading = false) }
                }
        }
    }

    fun startChatWithUser(sellerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Lấy thông tin người bán trước để hiện lên Header ngay
                val sellerData = authRepository.getUserById(sellerId)
                if (sellerData != null) {
                    _uiState.update { it.copy(otherUser = sellerData) }
                    
                    // 2. Sau đó mới lấy/tạo phòng chat
                    val currentUserData = authRepository.getCurrentUserData()
                    if (currentUserData != null) {
                        val roomId = repository.getOrCreateChatRoom(currentUserData, sellerData)
                        _uiState.update { it.copy(currentRoomId = roomId) }
                        observeMessages(roomId)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy thông tin Shop") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${e.message}") }
            }
        }
    }

    private fun observeMessages(roomId: String) {
        viewModelScope.launch {
            repository.getMessagesFlow(roomId)
                .catch { e -> 
                    _uiState.update { it.copy(errorMessage = "Lỗi tải tin nhắn: ${e.message}", isLoading = false) } 
                }
                .collect { msgs ->
                    _uiState.update { it.copy(messages = msgs, isLoading = false) }
                }
        }
    }

    fun sendMessage(message: String) {
        val state = _uiState.value
        val roomId = state.currentRoomId ?: return
        val senderId = auth.currentUser?.uid ?: return
        val receiverId = state.otherUser?.id ?: return
        
        if (message.isBlank()) return
        
        viewModelScope.launch {
            try {
                repository.sendMessage(roomId, senderId, receiverId, message)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Không thể gửi tin nhắn") }
            }
        }
    }
}
