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
    val ownerId: String = "",
    val shopName: String = "Cửa hàng sách", // Tên shop bán quyển này
    val shopAvatarUrl: String? = null,       // Ảnh đại diện shop
    val category: String = "Khác",
    val stock: Int = 0
)
