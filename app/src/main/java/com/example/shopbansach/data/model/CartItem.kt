package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class CartItem(
    @get:PropertyName("bookId")
    @set:PropertyName("bookId")
    var bookId: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("price")
    @set:PropertyName("price")
    var price: Long = 0,

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String? = null,

    @get:PropertyName("quantity")
    @set:PropertyName("quantity")
    var quantity: Int = 1,

    @get:PropertyName("author")
    @set:PropertyName("author")
    var author: String = "",

    @get:PropertyName("ownerId")
    @set:PropertyName("ownerId")
    var ownerId: String = "",
    
    @get:PropertyName("isSelected")
    @set:PropertyName("isSelected")
    var isSelected: Boolean = false,

    @get:PropertyName("stock")
    @set:PropertyName("stock")
    var stock: Int = 0 // Thêm trường stock để kiểm tra nhanh trong giỏ hàng
)
