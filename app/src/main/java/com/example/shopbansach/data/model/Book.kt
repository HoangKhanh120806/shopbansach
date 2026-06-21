package com.example.shopbansach.data.model

data class Book(
    val id: String = "",
    val title: String = "",
    val titleLowercase: String = "", 
    val author: String = "",
    val price: Long = 0,
    val rating: Double = 0.0,
    val pages: Int = 0,
    val synopsis: String = "",
    val imageUrl: String? = null,
    val ownerId: String = "",
    val shopName: String = "", // Để trống để bắt buộc lấy từ dữ liệu thực tế
    val shopAvatarUrl: String? = null,
    val category: String = "Khác",
    val stock: Int = 0,
    val reviewCount: Int = 0
)
