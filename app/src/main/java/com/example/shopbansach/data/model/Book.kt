package com.example.shopbansach.data.model

data class Book(
    val id: String = "", // Đổi sang String để dùng ID của Firestore
    val title: String = "",
    val author: String = "",
    val price: String = "",
    val rating: Double = 4.5,
    val pages: Int = 300,
    val synopsis: String = "",
    val imageUrl: String? = null, // Dùng cho ảnh từ Cloudinary
    val ownerId: String = "" // ID của người bán
)
