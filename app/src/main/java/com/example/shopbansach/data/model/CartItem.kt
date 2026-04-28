package com.example.shopbansach.data.model

data class CartItem(
    val bookId: String = "",
    val title: String = "",
    val price: Long = 0,
    val imageUrl: String? = null,
    val quantity: Int = 1,
    val author: String = "",
    val isSelected: Boolean = false // Mặc định là không tích
)
