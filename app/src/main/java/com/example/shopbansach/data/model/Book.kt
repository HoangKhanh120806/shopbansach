package com.example.shopbansach.data.model

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val price: String,
    val rating: Double = 4.5,
    val pages: Int = 300,
    val synopsis: String = "A fascinating story that explores deep human emotions and complex relationships...",
    val imageRes: Int? = null
)
