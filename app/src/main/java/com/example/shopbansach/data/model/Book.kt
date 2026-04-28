package com.example.shopbansach.data.model

data class Book(
    val id: String = "",
    val title: String = "",
    val titleLowercase: String = "", // Dùng cho việc tìm kiếm không phân biệt hoa thường
    val author: String = "",
    val price: Long = 0,
    val rating: Double = 0.0,
    val pages: Int = 0,
    val synopsis: String = "",
    val imageUrl: String? = null,
    val imagePublicId: String? = null, // Thêm trường này để quản lý ảnh trên Cloudinary
    val ownerId: String = "",
    val category: String = "Khác",
    val stock: Int = 0
)
