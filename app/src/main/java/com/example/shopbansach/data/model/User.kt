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
    val shopName: String? = null, // Tên shop riêng
    val shopAvatarUrl: String? = null, // Ảnh đại diện shop riêng
    val memberSince: String = "2024",
    val avatarUrl: String? = null, // Ảnh đại diện người dùng
    val role: UserRole = UserRole.USER
)
