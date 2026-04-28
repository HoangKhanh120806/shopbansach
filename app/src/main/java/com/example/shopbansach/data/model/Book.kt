package com.example.shopbansach.data.model

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val price: String = "",
    val rating: Double = 0.0,
    val pages: Int = 0,
    val synopsis: String = "",
    val imageUrl: String? = null,
    val ownerId: String = "",
    val category: String = "Khác",
    val stock: Int = 0
)
