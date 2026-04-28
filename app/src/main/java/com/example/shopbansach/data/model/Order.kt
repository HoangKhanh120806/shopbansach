package com.example.shopbansach.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Long = 0,
    val shippingAddress: Address = Address(),
    val paymentMethod: String = "cod",
    val status: String = "Chờ xác nhận",
    val createdAt: Long = System.currentTimeMillis()
)
