package com.example.shopbansach.data.model

enum class UserRole {
    USER,
    PENDING_SELLER, // Vai trò chờ Admin duyệt
    SELLER,
    ADMIN
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val shopName: String? = null,
    val shopAvatarUrl: String? = null,
    val memberSince: String = "2024",
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.USER
)
