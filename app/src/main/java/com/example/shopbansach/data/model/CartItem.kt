package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class CartItem(
    val bookId: String = "",
    val title: String = "",
    val price: Long = 0,
    val imageUrl: String? = null,
    val quantity: Int = 1,
    val author: String = "",
    
    @get:PropertyName("isSelected")
    @set:PropertyName("isSelected")
    var isSelected: Boolean = false
)
