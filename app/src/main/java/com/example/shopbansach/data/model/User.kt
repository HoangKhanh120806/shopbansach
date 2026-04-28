package com.example.shopbansach.data.model

enum class UserRole {
    USER,
    SELLER,
    ADMIN
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val memberSince: String = "2024",
    val avatarUrl: String? = null, // Đổi từ avatarRes sang avatarUrl để lưu link ảnh từ Cloud
    val role: UserRole = UserRole.USER
)
