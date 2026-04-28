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
    val avatarRes: Int? = null,
    val role: UserRole = UserRole.USER // Mặc định là USER
)
